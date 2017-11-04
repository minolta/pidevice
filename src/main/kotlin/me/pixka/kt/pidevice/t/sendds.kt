package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.ktbase.io.Configfilekt
import me.pixka.pibase.d.DS18value
import me.pixka.pibase.o.Infoobj
import me.pixka.pibase.s.Ds18valueService
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class Sendds(val io: Piio, val service: Ds18valueService, val http: HttpControl, val cfg: Configfilekt,
             val err: ErrorlogService, val dbcfg: DbconfigService) {

    var target = "http://localhost:5555/ds18value/add"
    private val mapper = jacksonObjectMapper()
    private var checkserver: String? = "http://localhost:5555/check"

    @Scheduled(initialDelay = 30*1000,fixedDelay = 60 * 1000)
    fun sendtask() {
        setup()
        if (http.checkcanconnect(checkserver!!)) {
            send()
        }
    }

    fun send() {
        val list = service.notInserver()
        for (item in list) {
            logger.debug("[sendds18b20]  " + item)
            try {
                val info = Infoobj()

               // info.ip = io.wifiIpAddress()
                info.mac = io.wifiMacAddress()
                info.ds18value = item

                val re = http.postJson(target, info)
                var entity = re.entity
                if (entity != null) {
                    val response = EntityUtils.toString(entity)
                    logger.debug("[sendds18b20] response : " + response)
                    val ret = mapper.readValue(response, DS18value::class.java)
                    if (ret.id != null) {
                        item.toserver = true
                        service.save(item)
                        logger.info("[sendds18b20] Send complete  ${item.id}")
                        //  dss.clean()
                    }
                }
            } catch (e: Exception) {
                logger.error("[sendds18b20] ERROR " + e.message)
                err.n("Sendds", "37-50", "${e.message}")
            }

        }
    }

    fun setup() {
        var host = dbcfg.findorcreate("hosttarget","http://pi1.pixka.me").value
        target = host+dbcfg.findorcreate("serverds18addtarget", ":5002/ds18value/add")?.value!!
        checkserver = host+dbcfg.findorcreate("servercheck", ":5002/run")?.value!!
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Sendds::class.java)
    }
}