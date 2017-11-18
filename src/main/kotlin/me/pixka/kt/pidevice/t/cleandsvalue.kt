package me.pixka.kt.pidevice.t

import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.Ds18valueService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class CleanDsvalue(val ds:Ds18valueService) {

    @Scheduled(fixedDelay = 60*60*1000)
    fun clean()
    {
        ds.cleanToserver()
        logger.debug("Clean DS value ")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CleanDsvalue::class.java)
    }
}