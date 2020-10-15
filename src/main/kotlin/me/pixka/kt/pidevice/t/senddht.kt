package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.s.DhtvalueService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.t.HttpPostTask
import me.pixka.log.d.LogService
import me.pixka.pibase.o.Infoobj
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class SendDht(val dhts: DhtvalueService, val http: HttpService,val lgs:LogService) {
    val om = ObjectMapper()
    @Scheduled(fixedDelay = 1000)
    fun run() {
        var target = System.getProperty("savedht")
        if (target == null)
            target = System.getProperty("piserver") + "/dht/add"
        val list = dhts.notInserver() as List<Dhtvalue>
        var mac:String? = ""
        if (list != null) {
            logger.debug("Found dht for send ${list.size}")
            for (dht in list) {
                try {
                    var obj = Infoobj()
                    obj.dhtvalue = dht
                    obj.mac = dht.pidevice?.mac
                    mac = obj.mac

                    logger.debug("Obj for send ${obj} URL ${target}")
                    try {

                        var value = http.post(target,obj,2000)
                        var d = om.readValue<Dhtvalue>(value)
                            dhts.delete(dht)
                    } catch (e: Exception) {
                        lgs.createERROR("${e.message} ", Date(),"Senddht",
                        "","","run",mac)
                        logger.error("Send Dht ERROR ${e.message}")
                    }
                } catch (e: Exception) {
                    lgs.createERROR("${e.message} ", Date(),"Senddht",
                            "","","run",System.getProperty("mac"))
                    logger.error("Error ${e.message}")
                }


            }
        }

        logger.debug("End send ds")
    }
    companion object {
        var logger = LoggerFactory.getLogger(SendDht::class.java)

    }

}