package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.ktbase.io.Configfilekt
import me.pixka.pibase.d.DS18value
import me.pixka.pibase.o.Infoobj
import me.pixka.pibase.s.Ds18valueService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Sendds(val task: SenddsTask) {


    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    fun sendtask() {
        try {

            var f = task.run()
            var count = 0
            while (true) {
                if (f!!.isDone) {
                    logger.info("Run commplete")
                    break
                }
                TimeUnit.SECONDS.sleep(1)
                count++

                if (count > 30) {
                    f.cancel(true)
                    logger.error("Timeout")
                }

            }
        } catch (e: Exception) {
            logger.error("Error send Sendds ${e.message}")
        }

        logger.debug("End Send ds")
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Sendds::class.java)
    }
}

@Component
class SenddsTask(val io: Piio, val service: Ds18valueService,
                 val http: HttpControl, val cfg: Configfilekt,
                 val err: ErrorlogService, val dbcfg: DbconfigService) {


    var target = "http://localhost:5555/ds18value/add"

    private var checkserver: String? = "http://localhost:5555/check"


    @Async("aa")
    fun run(): Future<Boolean>? {
        try {
            logger.info("Start Send DS data")
            setup()
            if (http.checkcanconnect(checkserver!!)) {
                logger.debug("Start to send Value ")
                send()
            }
        } catch (e: Exception) {
            logger.error("Error send Sendds ${e.message}")
        }

        logger.debug("End Send ds")
        return null
    }

    fun send() {
        try {
            val mapper = ObjectMapper()
            val list = service.notInserver()

            logger.debug("Values for send ${list.size}")
            for (item in list) {
                logger.debug("[sendds18b20]  " + item)
                var re: CloseableHttpResponse? = null
                try {
                    val info = Infoobj()

                    // info.ip = io.wifiIpAddress()
                    info.mac = io.wifiMacAddress()
                    info.ds18value = item

                    re = http.postJson(target, info)
                    var entity = re.entity
                    if (entity != null) {
                        val response = EntityUtils.toString(entity)
                        logger.debug("[sendds18b20] response : " + response)
                        val ret = mapper.readValue(response, DS18value::class.java)

                        item.toserver = true
                        service.save(item)
                        logger.info("[sendds18b20] Send complete  ${item.id}")
                        //  dss.clean()
                    }
                } catch (e: Exception) {
                    logger.error("[sendds18b20] ERROR " + e.message)
                    err.n("Sendds", "37-50", "${e.message}")
                } finally {
                    if (re != null)
                        re.close()
                }

            }
        } catch (e: Exception) {
            logger.debug(e.message)
        }
    }

    fun setup() {
        logger.debug("Setup...")
        var host = System.getProperty("piserver")
        if (host == null)
            host = dbcfg.findorcreate("hosttarget", "http://pi1.pixka.me").value

        target = host + "/ds18value/add"
        logger.debug("Target ${target}")
        checkserver = host + "/run"
        logger.debug("Check ${checkserver}")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(SenddsTask::class.java)
    }
}