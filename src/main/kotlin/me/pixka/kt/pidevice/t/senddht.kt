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
                        if (d != null) {
                            dhts.delete(dht)
                        }
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
//
//    val mapper = ObjectMapper()
//    @Throws(Exception::class)
//    fun postJson(url: String, obj: Any): CloseableHttpResponse {
//
//        try {
//            val provider = BasicCredentialsProvider()
//            val credentials = UsernamePasswordCredentials("USER_CLIENT_APP", "password")
//            provider.setCredentials(AuthScope.ANY, credentials)
//
//
//            //เปลียนมาใช้ ฺ Basic Auth
//            val client = HttpClientBuilder.create()
//                    .setDefaultCredentialsProvider(provider)
//                    .build()
//
//            val request = HttpPost(url)
//            val jvalue = mapper.writeValueAsString(obj)
//            logger.debug("JACK son:" + jvalue)
//            val params = StringEntity(jvalue)
//            request.entity = params
//            request.setHeader("Content-type", "application/json")
//            var re = client.execute(request)
//            // handle response here...
//
//            return re
//        } catch (ex: Exception) {
//            logger.error("HTTP POST " + ex.message)
//            throw ex
//
//            // handle exception here
//
//        } finally {
//
//            // Deprecated
//            // httpClient.getConnectionManager().shutdown();
//        }
//    }

    companion object {
        var logger = LoggerFactory.getLogger(SendDht::class.java)

    }

}