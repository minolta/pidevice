package me.pixka.kt.pibase.t

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Callable


class HttpGetTask(val url: String) : Callable<String?> {
    override fun call(): String? {
        var re: String? = null
        Thread.currentThread().name = "Call ${url} ${Date()}"
        try {
            val httpclient = HttpClients.createDefault()
            logger.debug("0 ${this} Client ${httpclient} URL:${url} ")
            val httpGet = HttpGet(url)
            val response1 = httpclient.execute(httpGet)

            try {
                logger.debug("1 Status : ${response1.statusLine}")
                val entity1 = response1.entity
                re = EntityUtils.toString(entity1)
                EntityUtils.consume(entity1)
                logger.debug("===================================================")
                logger.debug("2 [${re}] ")
                logger.debug("2.1 URL [${url}]")
                logger.debug("===================================================")
            } catch (e: Exception) {
                logger.error("3 ${e.message}")
            } finally {
                response1.close()
                httpclient.close()
                logger.debug("******************************************")
                logger.debug("4 Close connection return value [${re}]")
                logger.debug("******************************************")
                return re
            }

        } catch (e: Exception) {
            logger.error("5 ERROR: ${e.message}")
        }

        logger.error("6 ERROR ${re}")
        return re
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(HttpGetTask::class.java)
    }
}