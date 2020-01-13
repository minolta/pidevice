package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.GroupRunService
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Component
class Runhjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
                  val dhs: Dhtutil, val httpControl: HttpControl,
                  val dhtvalueService: DhtvalueService, val groups: GroupRunService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var list = loadjob()
            if (list != null)
                logger.debug("Job for Runhjobbyd1 ${list.size}")
            if (list != null) {
                for (job in list) {
                    logger.debug("Run ${job}")

                    var t = D1hjobWorker(job, dhtvalueService, dhs, httpControl, task)

                    if (groups.canrun(t)) {
                        if (task.checktime(job)) {
                            var run = task.run(t)
                            logger.debug("${job} RunJOB ${run}")
                        } else {
                            logger.error("${job} Not in time rang ")
                        }
                    } else {
                        logger.debug("${job} ********************** Somedeviceusewater ***************")
                    }
                }

            }
        } catch (e: Exception) {
            logger.error("Read h by d1 ERROR ${e.message}")
        }
    }

    var df = SimpleDateFormat("HH:mm")
    fun checktime(job: Pijob): Boolean {
        try {
//            df.timeZone = TimeZone.getTimeZone("+0700")
            var n = df.format(Date())

            var now = df.parse(n)
            logger.debug("checktime N:${n} now ${now} now time ${now.time}")
            logger.debug("checktime s: ${job.stimes} ${now} e:${job.etimes}")
            if (job.stimes != null && job.etimes != null) {
                var st = df.parse(job.stimes).time
                var et = df.parse(job.etimes).time
                logger.debug("checktime ${st} <= ${now} <= ${et}")
                if (st <= now.time && now.time <= et)
                    return true
            } else if (job.stimes != null && job.etimes == null) {
                var st = df.parse(job.stimes).time
                logger.debug("checktime ${st} <= ${now} ")
                if (st <= now.time)
                    return true
            } else if (job.stimes == null && job.etimes != null) {
                var st = df.parse(job.etimes).time
                logger.debug("checktime ${st} >= ${now}")
                if (st <= now.time)
                    return true
            } else {
                logger.debug("${job.name} checktime not set ")
                return true
            }
        } catch (e: Exception) {
            logger.error("checktime ${e.message}")
        }

        return false
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