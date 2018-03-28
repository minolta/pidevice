package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Message
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.s.MessageService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Component
@Profile("pi", "lite")
class Sendmessage(val task: SendmessageTask) {


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun sendtask() {
        try {

            var f = task.run()
            var count = 0
            while (true) {
                if (f!!.isDone) {
                    logger.info("Run commplete")
                    break
                }
                TimeUnit.SECONDS.sleep(1)
                count++

                if (count > 30) {
                    f.cancel(true)
                    logger.error("Timeout")
                }

            }
        } catch (e: Exception) {
            logger.error("Error send Sendds ${e.message}")
        }

        logger.debug("End Send ds")
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Sendmessage::class.java)
    }
}


@Component
@Profile("pi", "lite")
class SendmessageTask(val io: Piio, val service: MessageService,
                      val http: HttpControl,
                      val err: ErrorlogService, val dbcfg: DbconfigService) {


    var target = "http://localhost:5555/ds18value/add"
    private var checkserver: String? = "http://localhost:5555/check"


    @Async("aa")
    fun run(): Future<Boolean>? {
        try {
            logger.info("Start Send message data")
            setup()
            if (http.checkcanconnect(checkserver!!)) {
                logger.debug("Start to send Value ")
                send()
            }
        } catch (e: Exception) {
            logger.error("Error send SendMessage ${e.message}")
        }

        logger.debug("End Send Message")
        return null
    }

    fun send() {
        try {
            val mapper = ObjectMapper()
            val list = service.notsend()

            if (list != null) {
                logger.info("Send message to server ${list.size}")
                logger.debug("Values for send ${list.size}")
                for (item in list) {
                    logger.debug("[sendds18b20]  " + item)
                    var re: CloseableHttpResponse? = null
                    try {
                        var forsend = Message()
                        forsend.messagedate = item.messagedate
                        forsend.messagetype = item.messagetype
                        forsend.message = item.message
                        var p = PiDevice()
                        p.mac = io.wifiMacAddress()
                        forsend.pidevice = p

                        re = http.postJson(target, forsend)
                        var entity = re.entity
                        if (entity != null) {
                            val response = EntityUtils.toString(entity)
                            logger.debug("[sendmessage] response : " + response)
                            val ret = mapper.readValue(response, Message::class.java)
                            if (ret != null) {
                                item.toserver = true
                                //item.pidevice = null
                                service.save(item)
                                logger.info("[sendmessage] Send complete  ${item.id}")
                            }
                            //  dss.clean()
                        }
                    } catch (e: Exception) {
                        logger.error("[sendmessage] ERROR " + e.message)
                        err.n("Send message", "114", "${e.message}")
                    } finally {
                        if (re != null)
                            re.close()
                    }

                }
            }
        } catch (e: Exception) {
            logger.debug(e.message)
        }

    }

    fun setup() {
        logger.debug("Setup... Message Service")
        var host = System.getProperty("piserver")
        if (host == null)
            host = dbcfg.findorcreate("hosttarget", "http://pi1.pixka.me:5002").value

        target = host + "/message/add"
        logger.debug("Target ${target}")
        checkserver = host + "/run"
        logger.debug("Check ${checkserver}")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(SendmessageTask::class.java)
    }
}