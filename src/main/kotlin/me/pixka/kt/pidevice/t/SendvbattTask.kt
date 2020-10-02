package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.VbattService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.run.Status
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Sendvbatt(val service: VbattService, val httpService: HttpService) {
    val om = ObjectMapper()

    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    fun sendtask() {
        println("Send v batt " + Date())
        var target = System.getProperty("piserver") + "/vbatt/add"
        var list = service.nottoserver()
        if (list != null && !list.isEmpty()) {
            list.map {


                try {
                    var re = httpService.post(target, it,1000)
                    var status = om.readValue<Status>(re)
                    it.toserver = true
                    var v = service.save(it)
                } catch (e: Exception) {
                    logger.error("Send ds18vale error ${e.message}")
                }
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Sendvbatt::class.java)
    }
}