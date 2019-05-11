package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.TimeUtil
import me.pixka.kt.run.D1hjobWorker
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Component
//@Profile("pi")
class Runhjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
                  val dhs: Dhtutil, val httpControl: HttpControl,
                  val dhtvalueService: DhtvalueService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {

        var list = loadjob()
        if (list != null)
            logger.debug("Job for Runhjobbyd1 ${list.size}")
        if (list != null) {
            for (job in list) {
                logger.debug("Run ${job}")
//                if (checkrun(job) && checktime(job)) {
//                    var checkjob = checkgroup(job)
//                    if (checkjob != null) {
//                        logger.debug("Run this job ==== ${checkjob}")
                var t = D1hjobWorker(job, dhtvalueService, dhs, httpControl, task)
                var run = task.run(t)
                logger.debug("RunJOB ${run}")
//                TimeUnit.SECONDS.sleep(1)
//                    } else {
//                        logger.error("Some job in group runing now")
//                    }
//                } else {
//                    logger.debug("This job is run ${job}")
//                }
            }
        }
    }

    fun checkrun(job: Pijob): Boolean {
        logger.debug("Check job ${job}")
        var runs = task.runinglist
        for (run in runs) {
            if (run is D1hjobWorker) {
                var runid = run.getPijobid().toInt()
                if (job.id.toInt() == runid && run.isRun) {
                    logger.debug("Now this job is run ${job}")
                    return false
                }

            }
        }
        return true
    }

    fun checkgroup(job: Pijob): Pijob? {
        var runs = task.runinglist

        for (run in runs) {
            if (run is D1hjobWorker) {
                //ถ้า job รอแล้ว
                logger.debug("Wait status is ${run.waitstatus} RunGROUPID ${run.pijob.pijobgroup_id} " +
                        "JOBGROUPID ${job.pijobgroup_id} ")

                if (run.isRun && run.waitstatus) {
                    if (run.pijob.pijobgroup_id?.toInt() == job.pijobgroup_id?.toInt()) {
                        return null
                    }
                }
//                if (run.waitstatus == true && run.pijob.pijobgroup_id != null && job.pijobgroup_id != null && run.isRun) {
//                    var rungroupid = run.pijob.pijobgroup_id?.toInt()
//                    var jobgroupid = job.pijobgroup_id?.toInt()
//                    logger.debug("D1h ${rungroupid} == ${jobgroupid} ")
//                    if (jobgroupid == rungroupid) { //ถ้าอยู่ในกลุ่มเดียวกัน
//                        logger.error("D1h runing")
//                        return null
//                    }
//                }
//                //


            }
        }
        logger.debug("No Job in this group run ${job}")
        return job
    }

    var time = SimpleDateFormat("hh:mm:ss")
    fun checktime(job: Pijob): Boolean {
        if (job.stimes == null || job.etimes == null)
            return true
        var now = Date()
        var timeonlystring = time.format(now)
        var timenow = time.parse(timeonlystring)
        var starttime = time.parse(job.stimes)
        var enddate = time.parse(job.etimes)

        if (timenow.time >= starttime.time && timenow.time <= enddate.time)
            return true

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