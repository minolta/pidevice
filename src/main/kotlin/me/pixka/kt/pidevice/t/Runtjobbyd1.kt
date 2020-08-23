package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1tjobWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
//@Profile("pi")
class Runtjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
//                  val gpios: GpioService,
                  val dhs: Dhtutil, val httpControl: HttpControl, val psij: PortstatusinjobService,
                  val readUtil: ReadUtil) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {
        try {
            logger.debug("Start run ${Date()}")
            var list = loadjob()
            logger.debug("found job ${list?.size}")
            if (list != null) {
                for (job in list) {
                    try {
                        var testjob = pjs.findByRefid(job.runwithid)
                        logger.debug("")
                        var t = D1tjobWorker(job, readUtil, psij, testjob)
                        task.run(t)

//                        if (t.checktmp(job)) {
//                            var canrun = task.run(t)
//                            logger.debug("This job run JOB:${job.name} ==> ${canrun}")
//                        }
//                        else
//                            logger.debug("Job out of rang")
                    } catch (e: Exception) {
                        logger.error("${job.name} ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }


    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runtbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runtjobbyd1::class.java)
    }
}