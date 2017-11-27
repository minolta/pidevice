package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.DisplayService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Displayruning(val dps: DisplayService) {


    @Scheduled(fixedDelay = 1000)
    fun run() {

        logger.debug("Run Display status")
        var count = 0
        try {
            while (dps.lock) {
                TimeUnit.MILLISECONDS.sleep(200)
                count++
                if (count > 10) {
                    logger.error("Error Display timeout")
                    return
                }
            }
            if (!dps.lock) {
                var dot = dps.lockdisplay(this)
                dot.letter(2, '.'.toShort())
                TimeUnit.MILLISECONDS.sleep(500)
                dot.letter(2, ' '.toShort())
                TimeUnit.MILLISECONDS.sleep(500)
                dps.unlock(this)
                //  TimeUnit.SECONDS.sleep(1)
            } else {
                logger.error("Display device Busy")
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message}")
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Displayruning::class.java)
    }


}

