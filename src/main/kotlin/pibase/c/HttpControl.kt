package me.pixka.c

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.base.s.ErrorlogService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import java.io.IOException
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider



@Controller
class HttpControl(val err: ErrorlogService) {

    @Throws(IOException::class)
    operator fun get(s: String): String {
        val provider = BasicCredentialsProvider()
        val credentials = UsernamePasswordCredentials("USER_CLIENT_APP", "password")
        provider.setCredentials(AuthScope.ANY, credentials)


        //เปลียนมาใช้ ฺ Basic Auth
        val client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()
        val response1 = client.execute(
                HttpGet(s))
        val statusCode = response1.statusLine
                .statusCode



//        val httpclient = HttpClients.createDefault()
//        val httpGet = HttpGet(s)
       // val response1 = client.execute(httpGet)




        try {
            println(response1.statusLine)
            val entity1 = response1.entity
            val re = EntityUtils.toString(entity1)
            EntityUtils.consume(entity1)
            return re
        } catch (e: Exception) {
            logger.error("HTTP Control error :" + e.message)
            err.n("GET HTTPCONTROL", "27-31", "${e.message}")
            throw e
        } finally {
            logger.debug("Close connection get()")
            response1.close()
        }

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
            err.n("HttpControl", "49-55", "${ex.message}")
            throw ex

            // handle exception here

        } finally {

            // Deprecated
            // httpClient.getConnectionManager().shutdown();
        }
    }

    fun checkcanconnect(checkserver: String): Boolean {
        try {
            val re = get(checkserver)
            logger.debug("[checkconnection] connection ok :" + re)
            return true
        } catch (e: IOException) {
            logger.error("[checkconnection] Can not connect to server " + e.message)
            err.n("checkcanconnect", "73", "${e.message}")
        }

        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(HttpControl::class.java!!)
    }

}
