package me.pixka.kt.pidevice.t


import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DhtvalueService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1portjobWorker
import me.pixka.kt.run.GroupRunService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class RunportByd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
                  val dhs: Dhtutil, val httpControl: HttpControl,
                  val dhtvalueService: DhtvalueService, val groups: GroupRunService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        logger.debug("Start get task Runportbyd1 ${Date()}")
        try {
            var list = loadjob()
            if (list != null)
                logger.debug("Job for Runportbyd1 Port jobsize  ${list.size}")
            if (list != null) {
                for (job in list) {
                    logger.debug("Run Port  ${job}  Description :${job.description}")

                    var t = D1portjobWorker(job, pjs, dhs, httpControl, task)

                    if (task.checktime(job)) {
                        if (!t.checkCanrun()) {
                            t.state = "H not in ranger"
                        } else {
                            var run = task.run(t)
                            logger.debug("${job} RunJOB ${run}")

                        }
                    } else {
                        logger.debug("${job} Not in time rang ")
                        t.state = "Not in run in this time"
                    }
                }


            }


        } catch (e: Exception) {
            logger.error("Read h by d1 ERROR ${e.message}")
        }
    }

//    var df = SimpleDateFormat("HH:mm")
//    fun checktime(job: Pijob): Boolean {
//        try {
////            df.timeZone = TimeZone.getTimeZone("+0700")
//            var n = df.format(Date())
//
//            var now = df.parse(n)
//            logger.debug("checktime N:${n} now ${now} now time ${now.time}")
//            logger.debug("checktime s: ${job.stimes} ${now} e:${job.etimes}")
//            if (job.stimes != null && job.etimes != null) {
//                var st = df.parse(job.stimes).time
//                var et = df.parse(job.etimes).time
//                logger.debug("checktime ${st} <= ${now} <= ${et}")
//                if (st <= now.time && now.time <= et)
//                    return true
//            } else if (job.stimes != null && job.etimes == null) {
//                var st = df.parse(job.stimes).time
//                logger.debug("checktime ${st} <= ${now} ")
//                if (st <= now.time)
//                    return true
//            } else if (job.stimes == null && job.etimes != null) {
//                var st = df.parse(job.etimes).time
//                logger.debug("checktime ${st} >= ${now}")
//                if (st <= now.time)
//                    return true
//            } else {
//                logger.debug("${job.name} checktime not set ")
//                return true
//            }
//        } catch (e: Exception) {
//            logger.error("checktime ${e.message}")
//        }
//
//        return false
//    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runportbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunportByd1::class.java)
    }
}