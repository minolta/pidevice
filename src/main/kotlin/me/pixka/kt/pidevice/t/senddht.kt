package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.ktbase.io.Configfilekt
import me.pixka.pibase.d.Dhtvalue
import me.pixka.pibase.o.Infoobj
import me.pixka.pibase.s.DhtvalueService
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class SendDht(val io: Piio, val dhts: DhtvalueService, val cfg: Configfilekt,
              val err: ErrorlogService, val http: HttpControl, val dbcfg: DbconfigService) {
    private val mapper = jacksonObjectMapper()
    var target = "http://localhost:5002/dht/add"
    private var checkserver = "http://localhost:5002/run"

    @Scheduled(initialDelay = 60*1000,fixedDelay = 60 * 1000)
    fun run() {

        setup()

        if (http.checkcanconnect(checkserver)) {
            send()
        } else {
            logger.error("[dhtvaluesend] Can not connect to server : " + checkserver)
        }


    }

    fun send() {

        val list = dhts.notInserver() as List<Dhtvalue>

        logger.info("[dhtvaluesend send] Data for send : " + list?.size)


        for (item in list) {

            try {
                val info = Infoobj()

                //info.ip = io.wifiIpAddress()
                info.mac = io.wifiMacAddress()
                info.dhtvalue = item

                val re = http.postJson(target, info)
                val entity = re.entity
                if (entity != null) {
                    val response = EntityUtils.toString(entity)
                    logger.debug("[dhtvaluesend return status ${re}] ")
                    val ret = mapper.readValue(response, Dhtvalue::class.java)
                    if (ret.id != null) {
                        item.toserver = true
                        dhts.save(item)
                        logger.info("[dhtvalue ] Send complete  ${item}")
                        // dss.clean()
                    }
                }
            } catch (e: Exception) {
                logger.error("[dhtvaluesend ] cannot send : " + e.message)
                err.n("Senddht", "43-62", "${e.message}")
            }

        }

    }

    fun setup() {
        var host = dbcfg.findorcreate("hosttarget","http://pi1.pixka.me").value
        target = host+dbcfg.findorcreate("serverdhtaddtarget", ":5002/dht/add/").value!!
        checkserver = host+dbcfg.findorcreate("servercheck", ":5002/run").value!!
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(SendDht::class.java)
    }
}