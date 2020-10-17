package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Distance
import me.pixka.kt.pibase.d.DistanceService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Senddistance(val service: DistanceService, val httpService: HttpService, val lgs: LogService) {
    var target = System.getProperty("piserver") + "/adddistance"
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun send() {

        try {
            var forsend = service.all()

            if (forsend != null) {
                forsend.forEach {

                    var re = httpService.post(target, it, 5000)
                    var o = om.readValue<Distance>(re)
                    service.delete(it)
                }
            }
        } catch (e: Exception) {
//            lgs.createERROR("${e.message}", Date(), "Senddistance",
//                    "", "20", "send()", System.getProperty("mac"))
            logger.error(e.message)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Senddistance::class.java)
    }
}