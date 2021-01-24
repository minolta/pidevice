package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.OffpumpWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture


@Component
class RunoffPump(val pjs: PijobService, val findJob: FindJob, val httpService: HttpService,
                 val js: JobService, val checkTimeService: CheckTimeService,val mtp:MactoipService,
                 val task: TaskService, val ips: IptableServicekt, val lgs: LogService
) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Run off pump")
        try {
            var jobs = findJob.loadjob("offpump")
            logger.debug("Found job ${jobs?.size}")
            var mac: String? = null
            if (jobs != null) {
                for (job in jobs) {
                    mac = job.desdevice?.mac
                    if (checkTimeService.checkTime(job, Date()) && !task.checkrun(job)) {
                        var offpumpWorker = OffpumpWorker(job,mtp)
                        if (!task.run(offpumpWorker)) {
//                            logger.error("Can not Run ${job.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("runoffpumb ${e.message}")
            lgs.createERROR("${e.message}", Date(),
                    "RunoffPump", "", "", "run", ""
            )

        }


    }




         var logger = LoggerFactory.getLogger(RunoffPump::class.java)
}
