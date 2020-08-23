package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.VbattService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1readvoltWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReadVtask(val pjs: PijobService,
                val js: JobService,
                val task: TaskService, val readUtil: ReadUtil, val portstatusinjobService: PortstatusinjobService,
                val pss: VbattService, val ntf: NotifyService) {

    @Scheduled(fixedDelay = 5000)
    fun run() {
        try {
            var list = loadjob()
            if (list != null)
                logger.debug("Job for ReadVtask ${list.size}")

            if (list != null) {
                for (job in list) {

                    var job = D1readvoltWorker(job, readUtil, portstatusinjobService, pss,ntf)
                    task.run(job)
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR READ V ${e.message}")
        }
    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runreadvolt")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadVtask::class.java)
    }
}