package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import org.apache.http.HttpEntity
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Sendpressure(val service: PressurevalueService, val http: HttpControl) {
    val ex = ThreadPoolExecutor(
            2,
            10,
            10, // <--- The keep alive for the async task
            TimeUnit.SECONDS, // <--- TIMEOUT IN SECONDS
            ArrayBlockingQueue(100),
            ThreadPoolExecutor.AbortPolicy() // <-- It will abort if timeout exceeds
    )
    val mapper = ObjectMapper()
    var target = "http://endpoint.pixka.me:8081/pressure/add"
    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    fun send() {

        var nt = System.getProperty("piserver")
        if (nt != null)
            target = nt + "/pressure/add"
        logger.debug("Start run Send pressure to ${nt}")
        var list = service.findNottoserver()
        if (list != null) {

            for (p in list) {
                httpControlpost(target, p)
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
                val ret = mapper.readValue(response, PressureValue::class.java)
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

    fun taskpost(target: String, p: PressureValue) {
        var re: CloseableHttpResponse? = null
        try {

            var post = HttpPostTask(target, p)
            var f = ex.submit(post)
            var entity: HttpEntity? = null
            try {
                var re = f.get(5, TimeUnit.SECONDS)
                logger.debug("Re : [${re}]")
                entity = re.entity
                if (entity != null) {
                    val response = EntityUtils.toString(entity)
                    logger.debug("[Send pressure] response : " + response)
                    val ret = mapper.readValue(response, PressureValue::class.java)
                    p.toserver = true
                    service.save(p)
                    logger.info("[Send pressure] Send complete  ${p.id}")
                } else {
                    logger.error("Entity is null")
                }
            } catch (e: Exception) {
                logger.error("ENTITY [${entity}] ${e.message}")
            }

//
//                    re = http.postJson(target, p)
//
//                    var entity = re.entity
//                    if (entity != null) {
//                        val response = EntityUtils.toString(entity)
//                        logger.debug("[Send pressure] response : " + response)
//                        val ret = mapper.readValue(response, PressureValue::class.java)
//
//                        p.toserver = true
//                        service.save(p)
//                        logger.info("[Send pressure] Send complete  ${p.id}")
//                        //  dss.clean()
//                    }
        } catch (e: Exception) {
            logger.error("ERROR 2  ${e.message}")
        }
    }
}


class HttpPostTask(var url: String, var obj: Any) : Callable<CloseableHttpResponse> {

    val mapper = ObjectMapper()
    override fun call(): CloseableHttpResponse? {
        val provider = BasicCredentialsProvider()
        val credentials = UsernamePasswordCredentials("USER_CLIENT_APP", "password")
        provider.setCredentials(AuthScope.ANY, credentials)


        //เปลียนมาใช้ ฺ Basic Auth
        val client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()
        // instead
        var re: CloseableHttpResponse? = null
        try {

            val request = HttpPost(url)
            val jvalue = mapper.writeValueAsString(obj)
            logger.debug("JACK son:" + jvalue)
            val params = StringEntity(jvalue)
            request.entity = params
            request.setHeader("Content-type", "application/json")
            re = client.execute(request)
            return re
        } catch (ex: Exception) {
            logger.error("HTTP POST " + ex.message)

        } finally {

            //  client.close()
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(HttpPostTask::class.java)
    }
}
