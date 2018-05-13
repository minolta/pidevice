package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.CDWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@Profile("pi")
class Runcooldown(val js: JobService, val pjs: PijobService,
                  val gpios: GpioService, val ts: TaskService, val ss: SensorService,
                  val m: MessageService, val i: Piio, val ppp: PortstatusinjobService) {

    @Scheduled(initialDelay = 2000, fixedDelay = 30000)
    fun run() {
        logger.info("Run Cooldown")

        var job = js.findByName("cooldown") //สำหรับแสดงผล

        if (job == null) {
            RunDSDP.logger.error("JOB COOL DOWN NOT FOUND")
            return
        }

        var jobs = pjs.findJob(job.id)


        if (jobs != null) {
            logger.debug("Found Jobs ${jobs.size}")
            exe(jobs)


        } else
            logger.debug("Job not found ")

    }

    fun exe(jobs: List<Pijob>) {

        for (pj in jobs) {
            var c = CDWorker(pj, ss, gpios, m, i, ppp)
            ts.run(c)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runcooldown::class.java)
    }
}