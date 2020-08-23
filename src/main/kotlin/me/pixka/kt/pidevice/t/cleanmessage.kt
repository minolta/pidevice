package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.MessageService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
//@Profile("pi","lite")
class Cleanmessage(val service: MessageService) {

    @Scheduled(fixedDelay = 60*60*1000)
    fun clean()
    {
        service.cleanToserver()
        logger.debug("Clean Message value ")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CleanDsvalue::class.java)
    }
}