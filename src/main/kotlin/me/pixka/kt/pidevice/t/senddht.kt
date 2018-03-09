package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.ktbase.io.Configfilekt
import me.pixka.pibase.d.Dhtvalue
import me.pixka.pibase.o.Infoobj
import me.pixka.pibase.s.DhtvalueService
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Component
@Profile("pi","lite")
class SendDht(val task: SenddhtTask) {


    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 60 * 1000)
    fun run() {


        var f = task.run()
        var count = 0
        while (true) {
            if (f!!.isDone) {
                logger.info("Complete")
                break
            }

            TimeUnit.SECONDS.sleep(1)
            count++
            if (count > 30) {
                logger.error("Time out")
                f.cancel(true)
            }
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(SendDht::class.java)
    }
}

@Component
@Profile("pi","lite")
class SenddhtTask(val io: Piio, val dhts: DhtvalueService, val cfg: Configfilekt,
                  val err: ErrorlogService, val http: HttpControl, val dbcfg: DbconfigService) {


    private val mapper = ObjectMapper()
    var target = "http://localhost:5002/dht/add"
    private var checkserver = "http://localhost:5002/run"

    @Async("aa")
    fun run(): Future<Boolean>? {

        try {
            setup()

            if (http.checkcanconnect(checkserver)) {
                send()
            } else {
                logger.error("[dhtvaluesend] Can not connect to server : " + checkserver)
            }

            return AsyncResult(true)
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return null
    }

    fun send() {
        try {
            val list = dhts.notInserver() as List<Dhtvalue>

            logger.info("[dhtvaluesend send] Data for send : " + list?.size)


            for (item in list) {
                var re: CloseableHttpResponse? = null
                try {
                    val info = Infoobj()

                    //info.ip = io.wifiIpAddress()
                    info.mac = io.wifiMacAddress()
                    info.dhtvalue = item

                    re = http.postJson(target, info)
                    val entity = re.entity
                    if (entity != null) {
                        val response = EntityUtils.toString(entity)
                        logger.debug("[dhtvaluesend return ok ")
                        val ret = mapper.readValue(response, Dhtvalue::class.java)
                        if (ret.id != null) {
                            item.toserver = true
                            dhts.save(item)
                            logger.info("[dhtvalue ] Send complete ")
                            // dss.clean()
                        }
                    }
                } catch (e: Exception) {
                    logger.error("[dhtvaluesend ] cannot send : " + e.message)
                    err.n("Senddht", "43-62", "${e.message}")
                } finally {
                    if (re != null)
                        re.close()

                }

            }

        } catch (e: Exception) {
            logger.debug("Send error : ${e.message}")
        }
        logger.debug("End Send DHT")
    }

    fun setup() {
        var host = System.getProperty("piserver")
        if (host == null)
            host = dbcfg.findorcreate("hosttarget", "http://pi1.pixka.me").value
        target = host + "/dht/add/"
        checkserver = host + "/run"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(SenddhtTask::class.java)
    }
}