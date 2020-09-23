package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.ReadDhtWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class ReadDhtfromother(val http: HttpControl,
                       val pjs: PijobService,
                       val js: JobService, val task: TaskService,
                       val dhts: DhtvalueService,
                       val pds: PideviceService, val dhtutil: Dhtutil,
                       val findJob: FindJob, val httpService: HttpService,
                       val ips: IptableServicekt, val lgs: LogService) {

    val om = ObjectMapper()


    @Scheduled(fixedDelay = 2000)
    fun run(): Dhtvalue? {
        try {
            var list = findJob.loadjob("readdht")
            if (list != null)
                logger.debug("Job for read dht ${list.size}")

            if(list!=null)
            for (i in list) {
                try {
                    var rt = ReadDhtWorker(i, dhts, pds, httpService, ips,lgs)
                    task.run(rt)
                } catch (e: Exception) {
                    logger.error("Run " + e.message)
                }
            }
        } catch (e: Exception) {
            logger.error("Read DHT ERROR ${e.message}")
            lgs.createERROR("Read DHT ERROR ${e.message}", Date(),
                    "ReadDhtfromother", "", "53", "run")
        }
        return null
    }


    fun readDht(url: String): Dhtvalue? {
        try {
            var value = httpService.get(url)
            var dhtvalue = om.readValue<Dhtvalue>(value, Dhtvalue::class.java)
            logger.debug("Get value ${dhtvalue}")
            return dhtvalue
        } catch (e: Exception) {
            logger.error("GET DHT OTHER ${e.message}")
            lgs.createERROR("GET DHT OTHER ${e.message}", Date())
            throw e
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDhtfromother::class.java)
    }
}


