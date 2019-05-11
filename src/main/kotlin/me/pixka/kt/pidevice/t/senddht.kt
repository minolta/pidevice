package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.pibase.o.Infoobj
import me.pixka.pibase.s.DhtvalueService
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
//@Profile("pi", "lite")
class SendDht(val dhts: DhtvalueService, val http: HttpControl) {
    var t = Executors.newSingleThreadExecutor()

    @Scheduled(fixedDelay = 1000)
    fun run() {

        println("Date : ${Date()}")
        var target = System.getProperty("savedht")

        if (target == null)
            target = "http://pi1.pixka.me:8081/dht/add"

        val list = dhts.notInserver() as List<Dhtvalue>

        if (list != null) {
            logger.debug("Found dht for send ${list.size}")
            for (dht in list) {
                try {
                    var obj = Infoobj()
                    obj.dhtvalue = dht
                    obj.mac = dht.pidevice?.mac
                    logger.debug("Obj for send ${obj} URL ${target}")
                    var task = HttpPostTask(target, obj)
                    var f = t.submit(task)
                    var value = f.get(60, TimeUnit.SECONDS)
//                    var value = http.postJson(target, obj)
                    logger.debug("Return Save DHT ${value}")

                    if (value != null) {
                        dht.toserver = true
                        dhts.save(dht)
                    }
                } catch (e: Exception) {
                    logger.error("Error ${e.message}")
                }


            }
        }

        logger.debug("End send ds")
    }

    val mapper = ObjectMapper()
    @Throws(Exception::class)
    fun postJson(url: String, obj: Any): CloseableHttpResponse {

        try {
            val provider = BasicCredentialsProvider()
            val credentials = UsernamePasswordCredentials("USER_CLIENT_APP", "password")
            provider.setCredentials(AuthScope.ANY, credentials)


            //เปลียนมาใช้ ฺ Basic Auth
            val client = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(provider)
                    .build()

            val request = HttpPost(url)
            val jvalue = mapper.writeValueAsString(obj)
            logger.debug("JACK son:" + jvalue)
            val params = StringEntity(jvalue)
            request.entity = params
            request.setHeader("Content-type", "application/json")
            var re = client.execute(request)
            // handle response here...

            return re
        } catch (ex: Exception) {
            logger.error("HTTP POST " + ex.message)
            throw ex

            // handle exception here

        } finally {

            // Deprecated
            // httpClient.getConnectionManager().shutdown();
        }
    }

    companion object {
        var logger = LoggerFactory.getLogger(SendDht::class.java)

    }

}