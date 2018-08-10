package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Sendpressure(val service: PressurevalueService, val http: HttpControl) {

    var target = "http://endpoint.pixka.me:5002/pressure/add"
    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    fun send() {
        var list = service.findNottoserver()
        if (list != null) {
            val mapper = ObjectMapper()
            for (p in list) {
                var re: CloseableHttpResponse? = null
                try {

                    re = http.postJson(target, p)

                    var entity = re.entity
                    if (entity != null) {
                        val response = EntityUtils.toString(entity)
                        logger.debug("[Send pressure] response : " + response)
                        val ret = mapper.readValue(response, PressureValue::class.java)

                        p.toserver = true
                        service.save(p)
                        logger.info("[Send pressure] Send complete  ${p.id}")
                        //  dss.clean()
                    }
                } catch (e: Exception) {
                    logger.error(e.message)
                }
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Sendpressure::class.java)
    }
}