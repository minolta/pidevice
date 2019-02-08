package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.GasWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class RungasJob(val service: PijobService, val js: JobService, val pjs: PijobService,
                val ts: TaskService, val gpioService: GpioService,
                val readUtil: ReadUtil, val psij: PortstatusinjobService) {

    @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    fun run() {

        var toruns = loadjob()
        logger.debug("Job to run ${toruns}")
        if (toruns != null && toruns.size > 0) {
            for (j in toruns) {
                if (checktmp(j) && runwith(j)) {
                    var testjob = service.findByRefid(j.runwithid)
                    var task = GasWorker(j, gpioService, readUtil, psij,testjob,service)
                    ts.run(task)
                }

            }
        }

    }

    //สำหรับ test job ที่ run ว่า ok มันทำงานได้เปล่าถ้าใช่ก็ run ต่อไปได้
    fun runwith(p: Pijob): Boolean {
        logger.debug("Start runwidth")
        try {
            var testwidth = p.runwithid
            logger.debug("runwidth job refid ${testwidth}")
            if (testwidth == null)
                return true
            //ถ้ามี runwidthid
            var testjob = service.findByRefid(testwidth)
            logger.debug("runwidth Check job ${testjob}")
            if (testjob != null) {
                var re = readUtil.checktmp(testjob)
                logger.debug("runwidth Test result ${re}")
                return re
            }
        } catch (e: Exception) {
            logger.error("Error runwith() ${e.message}")
            throw  e
        }
        return false
    }

    fun checktmp(p: Pijob): Boolean {

        try {
            var tmp = readUtil.readTmpByjob(p)
            if (tmp != null) {
                var tl = p.tlow?.toFloat()
                var th = p.thigh?.toFloat()
                var v = tmp.toFloat()

                if (tl != null) {
                    if (tl <= v && v <= th!!) {
                        return true
                    }
                }

                return false

            }
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
        return false
    }

    fun loadjob(): List<Pijob>? {
        try {
            var job = js.findByName("rungas")
            if (job != null) {
                var jobtorun = pjs.findJob(job.id)
                return jobtorun
            }
            return null
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RungasJob::class.java)
    }
}