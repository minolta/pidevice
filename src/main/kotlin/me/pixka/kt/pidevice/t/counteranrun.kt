package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.CounterandrunWorker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * ใช้สำหรับ load counterandrun job มาทำงาน
 */
@Profile("pi", "lite")
@Component
class CounterandRun(var gi: GpioService, val m: MessageService,
                    val read: ReadUtil,
                    val i: Piio, val ts: TaskService, val js: JobService,
                    val pjs: PijobService, val ss: SensorService, val dss: DS18sensorService,
                    val ppp: PortstatusinjobService) {


    @Scheduled(initialDelay = 10000, fixedDelay = 5000)
    fun run() {

        var toruns = loadjob()
        logger.debug("Job to run ${toruns}")
        if (toruns != null && toruns.size > 0) {
            for (j in toruns) {

                if (check(j)) {
                    var task = CounterandrunWorker(j, gi, m, i, ppp, ss, dss,read)
                    if (task != null) {
                        var t = ts.checkalreadyrun(task)
                        logger.debug("Check job  ${task}  can ${t}")
                        if (t != null) {
                            logger.debug("Run this job ${t} #Runcouterjob")
                            ts.run(t)
                        }
                    }
                }
            }
        }

    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("counterandrun")
        if (job != null) {
            var jobtorun = pjs.findJob(job.id)
            logger.debug("Found ${jobtorun}")
            return jobtorun
        }
        logger.error("Not found Counterandrun ")
        return null
    }

    /**
     * ใช้สำหรับตรวจสอบ ว่า counter แต่ละอันสามารถทำงานได้หรือเปล่า
     */
    fun check(pijob: Pijob): Boolean {

        try {
            var value = read.readTmpByjob(pijob)

            if (value != null) {
                var v = value.toFloat()
                var l = pijob.tlow?.toFloat()
                var h = pijob.thigh?.toFloat()
                if (v >= l!! && v <= h!!)
                    return true
            }
            logger.error("Can not read temp")
            return false
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CounterandRun::class.java)
    }

}