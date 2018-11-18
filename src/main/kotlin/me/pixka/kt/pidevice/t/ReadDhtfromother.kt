package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PideviceService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
@Profile("pi", "lite")
class ReadDhtfromother(val http: HttpControl,
                       val pjs: PijobService,
                       val js: JobService,
                       val dhts: DhtvalueService,
                       val pds: PideviceService,
                       val ips: IptableServicekt) {

    val om = ObjectMapper()
    @Scheduled(fixedDelay = 5000)
    fun run(): Dhtvalue? {

        var list = loadjob()
        if (list != null)
            logger.debug("Job for read dht ${list.size}")
        if (list != null)
            for (i in list) {

                try {
                    var des = i.desdevice
                    logger.debug("read from ${des}")
                    if (des != null) {
                        var ip = mactoip(des.mac!!)
                        logger.debug("read from ip ${ip}")
                        if (ip != null) {
                            var url = "http://${ip.ip}/dht"
                            var dhtvalue = readDht(url)
                            logger.debug("Read dht value ${dhtvalue}")
                            if (dhtvalue != null) {

                                var pidevice = pds.findByMac(dhtvalue.ip!!)
                                dhtvalue.valuedate = Date()

                                dhtvalue.pidevice = pidevice
                                var d = dhts.save(dhtvalue)
                                logger.info("Save otherdht ${d}")
                                if (i.waittime != null)
                                    TimeUnit.SECONDS.sleep(i.waittime!!.toLong())

                            }

                        }
                    }
                } catch (e: Exception) {
                    logger.error("line 39 " + e.message)
                    throw e
                }
            }

        return null
    }

    @Async
    fun readDht(url: String): Dhtvalue? {

        try {
            var rep = http.get(url)
            var dhtvalue = om.readValue<Dhtvalue>(rep, Dhtvalue::class.java)
            return dhtvalue
        } catch (e: Exception) {
            logger.error("line 56 ${e.message}")
            throw e
        }
        return null
    }


    fun mactoip(mac: String): Iptableskt? {
        try {
            var ip = ips.findByMac(mac)
            return ip
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return null
    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("readdht")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDhtfromother::class.java)
    }
}