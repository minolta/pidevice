package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Devicecheckin
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Message
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.s.DevicecheckinService
import me.pixka.kt.pibase.s.MessagetypeService
import me.pixka.pibase.o.Infoobj
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


@Component
//@Profile("pi", "lite")
class Checkin(val c: CheckinTask,val notifyService: NotifyService) {

    fun checkinii()
    {

    }

    @Scheduled(initialDelay = 15000, fixedDelay = 60000)
    fun checkin() {
        logger.info("Checkin ")
       // notifyService.message("check in")
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
//@Profile("pi", "lite")
class CheckinTask(val io: Piio, val http: HttpControl,
                  val ds: DevicecheckinService,
                  val mtservice: MessagetypeService, val ips: IptableServicekt) {
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
            logger.debug("checkin ${i.ip}")
            re = http.postJson(target!!, i)
            logger.info("[checkin] Checkin already ")

            /**
             * Test Add message
             */

            val mapper = ObjectMapper()

            val entity = re.entity
            val responseString = EntityUtils.toString(entity, "UTF-8")
            logger.debug("checkin response:  ${responseString}")
            var ci = mapper.readValue(responseString, Devicecheckin::class.java)
            ci.pidevice = null
            logger.debug("[checkin] update password checkin ok..")
            return ci
        } catch (e: Exception) {
            logger.error("[checkin] Can not checkin ${e.message}")
        } finally {
            re?.close()
        }

        return null
    }

    fun testmessage() {
        var mess = Message()
        var p = PiDevice()
        p.mac = io.wifiMacAddress()
        mess.pidevice = p
        mess.message = " test Message"
        mess.messagedate = Date()
        mess.messagetype = mtservice.findOrCreate("Test")
        logger.info("Push to server")
        var re = http.postJson(System.getProperty("piserver") + "/message/add", mess)
        logger.debug("checkininfo ${re}")
    }

    fun setup() {

        host = System.getProperty("piserver", "http://pi1.pixka.me")
        target = host + "/checkin"
        logger.debug("checkininfo Check in target ${target}")

    }

}