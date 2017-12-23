package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.ktbase.io.Configfilekt
import me.pixka.pibase.d.Devicecheckin
import me.pixka.pibase.o.Infoobj
import me.pixka.pibase.s.DevicecheckinService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


@Component
@Profile("pi")
class Checkin(val c: CheckinTask) {


    @Scheduled(initialDelay = 15000, fixedDelay = 60000)
    fun checkin() {
        logger.info("Checkin ")
        var f = c.run()
        var count = 0
        while (true) {
            if (f!!.isDone) {

                var d = f.get()
                logger.debug("Check in Done ID: ${d.id}")
                break
            }

            TimeUnit.SECONDS.sleep(1)
            count++

            if (count > 5) {//time out

                f.cancel(true)
                logger.error("Time out")
            }

        }

        logger.info("End Check job")


    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Checkin::class.java)
    }
}

@Component
class CheckinTask(val configfile: Configfilekt, val erl: ErrorlogService, val io: Piio, val http: HttpControl,
                  val ds: DevicecheckinService, val dbcfg: DbconfigService) {
    var target: String? = null
    var host: String? = null

    companion object {
        internal var logger = LoggerFactory.getLogger(CheckinTask::class.java)
    }

    @Async("aa")
    fun run(): Future<Devicecheckin>? {
        try {
            setup()
            var v = check()
            return AsyncResult(v)
        } catch (e: Exception) {
            logger.error(e.message)
        }

        return null
    }

    fun check(): Devicecheckin? {
        var re: CloseableHttpResponse? = null
        try {
            logger.info("[checkin] start : target : " + target)
            val i = Infoobj()
            i.ip = io.wifiIpAddress()
            i.mac = io.wifiMacAddress()
            i.password = UUID.randomUUID().toString() // สร้าง ยฟหหไนพก
            // สำหรับ ให้
            // Server
            // ใช้สำหรับติดต่อกับเรา

            re = http.postJson(target!!, i)
            logger.info("[checkin] Checkin already ")
            val mapper = ObjectMapper()

            val entity = re.entity
            val responseString = EntityUtils.toString(entity, "UTF-8")
           // logger.debug("checkin response:  ${responseString}")

            var ci = mapper.readValue(responseString, Devicecheckin::class.java)
            ci.pidevice = null
            var v = ds.save(ci)
            logger.debug("[checkin] update password checkin ok..")
            return v
        } catch (e: Exception) {
            logger.error("[checkin] Can not checkin ${e.message}")
        } finally {
            re?.close()
        }

        return null
    }

    fun setup() {
        host = dbcfg.findorcreate("hosttarget", "http://pi1.pixka.me").value
        target = host + dbcfg.findorcreate("checkintarget", ":5002/checkin").value

    }

}