package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1TimerWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class runD1Timer(val pjs: PijobService,
                 val js: JobService,
                 val task: TaskService,val ips:IptableServicekt,
//                  val gpios: GpioService,
                 val dhs: Dhtutil, val httpControl: HttpControl, val psij: PortstatusinjobService,
                 val readUtil: ReadUtil) {
    val om = ObjectMapper()
    @Scheduled(fixedDelay = 5000)
    fun run() {

        try {
            var list = loadjob()
            if (list != null)
                logger.debug("Job for Runhjobbyd1 ${list.size}")
            if (list != null) {
                for (job in list) {
                    try {
                        logger.debug("Run ${job}")
                        var t = D1TimerWorker(job,ips, readUtil, psij)
                        var run = task.run(t)
                        logger.debug("RunJOB ${run}")
                    } catch (e: Exception) {
                        logger.error("Error ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error ${e.message}")

        }
    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runtimerbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(runD1Timer::class.java)
    }
}