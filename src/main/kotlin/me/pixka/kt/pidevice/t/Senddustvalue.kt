package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.run.Status
import me.pixka.log.d.LogService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Senddustvalue(val server: PmService, val httpService: HttpService,val lgs:LogService) {

    val om = ObjectMapper()
    var mac = System.getProperty("mac")

    @Scheduled(fixedDelay = 5000)
    fun run() {
        var host = System.getProperty("piserver")
        var datas = server.findByToServer(false)
        if (datas != null) {
            datas.forEach {

                try {
                    var h = httpService.post(host + "/pm/add", it,2000)
                    var r = om.readValue<Status>(h)
//                    it.toserver = true
//                    server.save(it)
                    server.delete(it)
                } catch (e: Exception) {
                    lgs.createERROR("${e.message}", Date(),
                    "Senddustvalue","","26","run",mac)
                }

            }
        }
    }

}