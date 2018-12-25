package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.CountdownDisplayWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CoundownDisplayTask(val display: DisplayService, val pjs: PijobService,
                          val js: JobService, val taskService: TaskService, val sensorService: SensorService,
                          val notifyService: NotifyService) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        var jobs = loadjob()

        if (jobs != null) {
            logger.debug("Found ${jobs.size}")
            for (job in jobs) {
                var task = CountdownDisplayWorker(job, sensorService, display,notifyService)
                var canrun = taskService.run(task)
                logger.debug("Run ${job.id} ${canrun}")
            }
        }

    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("countdowndisplay")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CoundownDisplayTask::class.java)
    }
}