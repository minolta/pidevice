package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
class Checkin(val notifyService: NotifyService) {
    @Scheduled(fixedDelay = 60000)
    fun checkin() {
        logger.info("Checkin ")
    }
    companion object {
        internal var logger = LoggerFactory.getLogger(Checkin::class.java)
    }
}

