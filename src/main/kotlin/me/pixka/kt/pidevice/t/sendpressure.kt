package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pibase.t.HttpPostTask
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
//@Profile("pi")
class Sendpressure(val service: PressurevalueService, val http: HttpControl) {
    val om = ObjectMapper()
    var target = "http://endpoint.pixka.me:8081/pressure/add"
    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    fun send() {

        var nt = System.getProperty("piserver")
        if (nt != null)
            target = nt + "/pressure/add"
        logger.debug("Start run Send pressure to ${nt}")
        var list = service.findNottoserver()
        if (list != null) {
            var t = Executors.newSingleThreadExecutor()
            logger.debug("Found pressure to send ${list.size}")
            for (p in list) {
//                httpControlpost(target, p)
                var h = HttpPostTask(target, p)
                var f = t.submit(h)
                try {
                    var re = f.get(5, TimeUnit.SECONDS)
                    if (re.statusLine.statusCode == 200) {
                        p.toserver = true
                        var sp = service.save(p)
                        logger.debug("Send pressure ${sp}")
                    } else {
                        logger.error("Send pressure error ${re.statusLine.statusCode}")
                    }
                } catch (e: Exception) {
                    logger.error("Send pressure error ${e.message}")
                }
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Sendpressure::class.java)
    }

    fun httpControlpost(target: String, p: PressureValue) {
        try {
            var re = http.postJson(target, p)

            var entity = re.entity
            logger.debug("Reponse ${re}  Entity : ${entity}")
            if (entity != null) {
                val response = EntityUtils.toString(entity)
                logger.debug("[Send pressure] response : " + response)
                val ret = om.readValue(response, PressureValue::class.java)
                p.toserver = true
                service.save(p)
                logger.info("[Send pressure] Send complete  ${p.id}")
                //  dss.clean()
            } else
                logger.error("Entity is null")
        } catch (e: Exception) {
            logger.error("HttpControl ${e.message}")
        }
    }


}

