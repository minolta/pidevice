package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.PressurevalueService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CleanPressure(val ps: PressurevalueService) {

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun clean() {
        ps.clean()
        logger.debug("Clean DS value ")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CleanDsvalue::class.java)
    }
}