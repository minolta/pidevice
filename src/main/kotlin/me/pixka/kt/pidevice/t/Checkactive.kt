package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.CheckActiveWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Checkactive(val js: JobService, val pjs: PijobService, val task: TaskService, val ps: PortstatusinjobService,
                  val ips: IptableServicekt, val ntfs: NotifyService) {
    companion object {
        internal var logger = LoggerFactory.getLogger(Checkactive::class.java)
    }

    @Scheduled(fixedDelay = 5000)
    fun run() {
        try {
            var jobs = loadjob()
            if (jobs != null) {
                logger.debug("Found checkactive ${jobs.size} ")
                for (j in jobs) {
                    logger.debug("Run ${j.name}")
                    var t = CheckActiveWorker(j, ps, ips, ntfs)
                    var run = task.run(t)
                    logger.debug("Run ${run}")
                    if (run)
                        logger.debug("Run job checkactive ${j.name}")
                    else
                        logger.warn("Can not run job ${j.name}")
                }

            }
        } catch (e: Exception) {
            logger.error("Run checkactive error ${e.message}")
        }
    }


    fun loadjob(): List<Pijob>? {
        try {
            var job = js.findByName("checkactive")
            if (job != null) {

                var jobs = pjs.findJob(job.id)
                logger.debug("Found Job !! ${jobs}")
                return jobs
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
        logger.error("Not have Job checkactive")
        throw Exception("Not have JOB")
    }

}