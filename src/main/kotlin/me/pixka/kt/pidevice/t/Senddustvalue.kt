package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pidevice.worker.DisplaytmpWorker
import me.pixka.kt.run.Status
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
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
                    var h = httpService.post(host + "/pm/add", it,10000)
                    var r = om.readValue<Status>(h)
                    server.delete(it)
                } catch (e: Exception) {
                    lgs.createERROR("${e.message}", Date(),
                    "Senddustvalue","","26","run",mac)
                    logger.error(e.message)
                }

            }
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Senddustvalue::class.java)
    }

}