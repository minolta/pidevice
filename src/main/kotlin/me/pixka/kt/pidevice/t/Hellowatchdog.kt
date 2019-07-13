package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.GpioService
import net.sf.ehcache.util.TimeUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Hellowatchdog(val gpioService: GpioService) {

    @Scheduled(fixedRate = 1000)
    fun run() {

        logger.debug("Say hello ${Date()}")
        try {
            var p3 = gpioService.getDigitalPin("p3")

            if (p3 != null) {
                gpioService.setPort(p3, true)
                TimeUnit.SECONDS.sleep(1)
                gpioService.setPort(p3, false)
                TimeUnit.SECONDS.sleep(1)
            } else
                logger.error("P3 not found")
        } catch (e: Exception) {
            logger.error(e.message)

        }

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Hellowatchdog::class.java)
    }
}