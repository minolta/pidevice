package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.DWK
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.Status
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class RunonPump(val pjs: PijobService, val checkTimeService: CheckTimeService,
                val js: JobService, val ips: IptableServicekt,
                val task: TaskService, val findJob: FindJob, val httpService: HttpService,
                val lgs: LogService) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Run on pump")
        try {
            var jobs = findJob.loadjob("onpump")
            if (jobs != null) {
                logger.debug("Job size ${jobs.size}")

                for (job in jobs) {
                    if (checkTimeService.checkTime(job, Date()) && !task.checkrun(job)) {
                        var ip = ips.findByMac(job.desdevice?.mac!!)
                        if (ip != null && !task.run(OnpumbWorker(job, httpService, ip,lgs))) {
//                          logger.error("Can not Run ${job.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(), "RunonPump", "",
                    "", "Run", System.getProperty("mac"))
            logger.error("On pump ${e.message}")
        }


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunonPump::class.java)
    }

}


class OnpumbWorker(var job: Pijob, val httpService: HttpService, var ip: Iptableskt?,var lgs:LogService)
    : DWK(job), Runnable {

    val om = ObjectMapper()


    override fun run() {
        try {
            startRun = Date()
            var ip = ip?.ip
            status = "call url http://${ip}/on"
            try {
                var re = httpService.getNoCache("http://${ip}/on",2000)
                var s = om.readValue<Status>(re)
                status = "on pumb is ok ${s.status} Uptime ${s.uptime}"
            } catch (e: Exception) {
                logger.error("on pumb error  onpump ${e.message} ${pijob.name}")
                lgs.createERROR("${e.message}",Date(),
                "RunonPump",pijob.name,"","",pijob.desdevice?.mac,pijob.refid)
                status = "on pumb error  onpump ${e.message} ${pijob.name}"
                isRun = false

            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            lgs.createERROR("${e.message}",Date(),
            pijob.name,"","","",pijob.desdevice?.mac,pijob.refid)
            status = "offpump ${e.message} ${pijob.name}"
            isRun = false //ออกเลยที่มีปัญฆา
        }
        exitdate = findExitdate(pijob)
    }
      var logger = LoggerFactory.getLogger(OnpumbWorker::class.java)


}