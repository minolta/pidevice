package me.pixka.kt.pibase.t

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable


class HttpPostTask(var url: String, var obj: Any) : Callable<CloseableHttpResponse> {

    val mapper = ObjectMapper()
    override fun call(): CloseableHttpResponse? {
        val httpClient = HttpClients.createDefault() // Use this
        // instead
        var re: CloseableHttpResponse? = null
        val request = HttpPost(url)
        try {


            val jvalue = mapper.writeValueAsString(obj)
            logger.debug("JACK son:" + jvalue)
            val params = StringEntity(jvalue)
            request.entity = params
            request.setHeader("Content-type", "application/json")
            re = httpClient.execute(request)
            return re
        } catch (ex: Exception) {
            logger.error("HTTP POST " + ex.message)

        } finally {

            // Deprecated
            // httpClient.getConnectionManager().shutdown();\

            httpClient.close()
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(HttpPostTask::class.java)
    }
}