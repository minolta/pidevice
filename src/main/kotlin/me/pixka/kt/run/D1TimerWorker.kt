package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class D1TimerWorker(val p: Pijob,
                    val readvalue: ReadUtil, val pijs: PortstatusinjobService, var test: Pijob? = null)
    : DefaultWorker(p, null, readvalue, pijs, D1tjobWorker.logger) {

    //    var httpControl = HttpControl()
    override fun run() {
        isRun = true
        startRun = Date()
        var t = readvalue.readTmpByjob(p)
        status = "T : ${t}"
        if (t != null)
            if (checkrang(t)) {
                //go !!
                status = "T in rang"
                try {
                    if (checkhigh()) {
                        //ใช้ข้อมูลของ port run
                        var list = pijs.findByPijobid(p.id)
                        if (list != null) {
                            status = "Set remote port"
                            setRemoteport(list as List<Portstatusinjob>)
                        }
                        status = "no have remote port"
                        isRun = false

                    }
                } catch (e: Exception) {
                    logger.error(e.message)
                    status = e.message
                    isRun = false
                    throw e

                }
            }

        status = "Out of rang"
        isRun = false
    }


    fun checkhigh(): Boolean {
        try {
            var timeout = 120 //สองนาที
            var t = readvalue.readTmpByjob(p)
            while (checkrang(t!!)) {
                val v = readvalue.readTmpByjob(p)
                if (v != null) {
                    var tt = v.toFloat()
                    if (v.toFloat() >= tt) {
                        return true
                    }


                }

                TimeUnit.SECONDS.sleep(1) // รอ 1 วินาที
                timeout--

                if (timeout <= 0) {
                    status = "Check high time out"
                    throw Exception("Check high time out")
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            status = e.message
            throw e
        }
        return false
    }

    fun checkrang(t: BigDecimal): Boolean {

        val l = p.tlow?.toFloat()
        val h = p.thigh?.toFloat()
        val v = t.toFloat()
        if (l != null && h != null) {
            if (l <= v && v <= h) {
                return true
            }
        }
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(D1TimerWorker::class.java)
    }

    fun get(s: String): String {
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
//            err.n("GET HTTPCONTROL", "27-31", "${e.message}")
            throw e
        } finally {
            logger.debug("Close connection get()")
            response1.close()
        }

    }
}
