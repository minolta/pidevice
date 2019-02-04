package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1hjobWorker
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class Runhjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
                  val gpios: GpioService,
                  val dhs: Dhtutil, val httpControl: HttpControl,
                  val dhtvalueService: DhtvalueService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {

        var list = loadjob()
        if (list != null)
            logger.debug("Job for Runhjobbyd1 ${list.size}")
        if (list != null) {
            for (job in list) {
                logger.debug("Run ${job}")
                var t = D1hjobWorker(job, gpios, dhtvalueService, dhs, httpControl)
                var run = task.run(t)
                logger.debug("RunJOB ${run}")
            }
        }
    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runhbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runhjobbyd1::class.java)
    }
}