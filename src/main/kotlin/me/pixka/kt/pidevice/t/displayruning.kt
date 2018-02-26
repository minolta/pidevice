package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.DisplayService
import me.pixka.pi.io.Dotmatrix
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

        try {

            if (!dps.lock) {
                var dot = lockdisplay()
                if (dot != null) {
                    dot.letter(2, '.'.toShort())
                    unlock()
                    TimeUnit.MILLISECONDS.sleep(500)
                    dot = lockdisplay()
                }
                if (dot != null) {
                    dot.letter(2, ' '.toShort())
                    unlock()
                    TimeUnit.MILLISECONDS.sleep(500)

                }
                //  TimeUnit.SECONDS.sleep(1)
            } else {
              //  logger.error("Display device Busy")
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message}")
        }

    }

    fun lockdisplay(): Dotmatrix? {
        var count = 0
        while (dps.lock) {
            TimeUnit.MILLISECONDS.sleep(200)
            count++
            if (count > 10) {
                logger.error("Error Display timeout")
                return null
            }
        }
        return dps.lockdisplay(this)
    }

    fun unlock() {
        dps.unlock(this)
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Displayruning::class.java)
    }


}

