package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pibase.s.HttpService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Sendpressure(val service: PressurevalueService, val httpService: HttpService) {
    val om = ObjectMapper()
    var target = "http://endpoint.pixka.me:8081/pressure/add"
    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun send() {

        var nt = System.getProperty("piserver")
        if (nt != null)
            target = nt + "/pressure/add"
        logger.debug("Start run Send pressure to ${nt}")
        var list = service.findNottoserver()
        if (list != null) {
            for (p in list) {
                try {
                    var re = httpService.post(target,p,2000)
                    var result = om.readValue<PressureValue>(re)
//                    p.toserver = true
                    service.delete(p)
                } catch (e: Exception) {
                    logger.error("Send pressure error ${e.message} ${target} ${p}")
                }
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Sendpressure::class.java)
    }


}

