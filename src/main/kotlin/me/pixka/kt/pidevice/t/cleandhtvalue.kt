package me.pixka.kt.pidevice.t

import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CleanDhtvalue(val dhts:DhtvalueService) {

    @Scheduled(cron = "0 0 4 * * *")
    fun clean()
    {
        dhts.cleanToserver()
        logger.info("Clean DHT value")

    }
    companion object {
        internal var logger = LoggerFactory.getLogger(CleanDhtvalue::class.java)
    }
}