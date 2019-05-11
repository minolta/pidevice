package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.ReadDhtWorker
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PideviceService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
//@Profile("pi", "lite")
class ReadDhtfromother(val http: HttpControl,
                       val pjs: PijobService,
                       val js: JobService, val task: TaskService,
                       val dhts: DhtvalueService,
                       val pds: PideviceService, val dhtutil: Dhtutil,
                       val taskservice: TaskService,
                       val ips: IptableServicekt) {

    val om = ObjectMapper()


    @Scheduled(fixedDelay = 2000)
    fun run(): Dhtvalue? {

        var list = loadjob()
        if (list != null)
            logger.debug("Job for read dht ${list.size}")
        else {
            logger.error("Not job for run ")
            throw Exception("JOB Readdht not found")
        }

        for (i in list) {

            try {

                var rt = ReadDhtWorker(i, dhtutil, dhts)
                task.run(rt)

//                    var des = i.desdevice
//                    logger.debug("read from ${des}")
//                    if (des != null) {
//                        var ip = mactoip(des.mac!!)
//                        logger.debug("read from ip ${ip}")
//                        if (ip != null) {
//                            var url = "http://${ip.ip}/dht"
//                            logger.debug("Read DHT url ${url}")
//                            var dhtvalue = readDht(url)
//                            logger.debug("Read dht value ${dhtvalue}")
//                            if (dhtvalue != null) {
//                                dhtvalue.valuedate = Date()
//                                dhtvalue.pidevice = des
//                                var d = dhts.save(dhtvalue)
//                                logger.info("Save otherdht ${d}")
//                                if (i.waittime != null)
//                                    TimeUnit.SECONDS.sleep(i.waittime!!.toLong())
//                            }
//                            else
//                            {
//                                logger.error("Dhtvalue is null")
//                            }
//                        }
//                    }
            } catch (e: Exception) {
                logger.error("Run " + e.message)
//                    throw e
            }
        }
        return null
    }


    fun readDht(url: String): Dhtvalue? {

        try {
            var get = HttpGetTask(url)
            var t = Executors.newSingleThreadExecutor()
            var f = t.submit(get)
            var value = f.get(20, TimeUnit.SECONDS)
            var dhtvalue = om.readValue<Dhtvalue>(value, Dhtvalue::class.java)
            logger.debug("Get value ${dhtvalue}")
            return dhtvalue
        } catch (e: Exception) {
            logger.error("GET DHT OTHER ${e.message}")
            throw e
        }
        return null
    }


    fun mactoip(mac: String): Iptableskt? {
        try {
            var ip = ips.findByMac(mac)
            return ip
        } catch (e: Exception) {
            logger.error("Read dht Macto ip" + e.message)
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


