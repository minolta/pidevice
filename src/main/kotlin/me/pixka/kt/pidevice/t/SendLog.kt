package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import me.pixka.log.d.Logsevent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendLog(val service: LogService, val httpService: HttpService) {
    var om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun send() {
        try {
            var server = System.getProperty("piserver")
            var toserver = service.toServer(false)
            if (toserver != null) {
                toserver.forEach {
                    if (it.mac == null) {
                        it.mac = System.getProperty("mac")
                    }

                    var re = httpService.post(server + "/addlog", it, 2000)
                    var ret = om.readValue<Logsevent>(re)
                    service.delete(it)
                }
            }


        } catch (e: Exception) {
//            service.createERROR("ERROR ${e.message}", Date(), "Sendlog", "",
//                    "38", "send")
            logger.error(e.message)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(SendLog::class.java)
    }
}