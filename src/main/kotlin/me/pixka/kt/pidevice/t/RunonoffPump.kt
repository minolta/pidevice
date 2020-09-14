package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.OffpumpWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture


@Component
class RunoffPump(val pjs: PijobService, val findJob: FindJob, val httpService: HttpService,
                 val js: JobService,val checkTimeService: CheckTimeService,
                 val task: TaskService, val ips: IptableServicekt
) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Run off pump")
        try {
            var jobs = findJob.loadjob("offpump")
            logger.debug("Found job ${jobs?.size}")

            if (jobs != null) {
                for (job in jobs) {
                    if(checkTimeService.checkTime(job, Date()) && !task.checkrun(job)) {
                        var offpumpWorker = OffpumpWorker(job, httpService, ips)
                        if(!task.run(offpumpWorker))
                        {
                            logger.error("Can not Run ${job.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("runoffpumb ${e.message}")

        }


    }



    fun Checktime(job: Pijob) {
        CompletableFuture.supplyAsync {
            var intime = checkTimeService.checkTime(job, Date())
            intime
        }.thenApply {
            if (it) {
                var haverun = task.runinglist.find {
                    it.getPijobid() == job.id && it.runStatus()
                }
                if (haverun == null) {
                    var offpumpWorker = OffpumpWorker(job, httpService, ips)
                    task.run(offpumpWorker)
                }
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunoffPump::class.java)
    }
}
