package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1pjobWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
//@Profile("pi")
class Runpjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService, val readUtil: ReadUtil
) {
    val om = ObjectMapper()
    @Scheduled(fixedDelay = 1000)
    fun run() {

        var list = loadjob()
        if (list != null)
            logger.debug("Job for Runhjobbyd1 ${list.size}")
        if (list != null) {
            for (job in list) {
                logger.debug("Run ${job}")
                var c = checkrunwith(job)
                if (c != null && c) {
                    var t = D1pjobWorker(job, readUtil)
                    var run = task.run(t)
                    logger.debug("RunJOB ${run}")
                }
            }
        }
    }

    fun checkrunwith(job: Pijob): Boolean? {
        if (job.runwithid == null)
            return true
        var testjob = pjs.findByRefid(job.runwithid)
        return runwith(job)
    }

    //สำหรับ test job ที่ run ว่า ok มันทำงานได้เปล่าถ้าใช่ก็ run ต่อไปได้
    fun runwith(testjob: Pijob?): Boolean? {
        try {
            //ถ้ามี runwidthid
            logger.debug("runwidth Check job ${testjob}")
            if (testjob != null) {
                var re = readUtil.checktmp(testjob)
                return re
            }
            return true // ถ้าไม่กำหนด run with ก็ run เลย

        } catch (e: Exception) {
            logger.debug("Error Check ${e.message}")
        }
        return false
    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runpbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runpjobbyd1::class.java)
    }
}