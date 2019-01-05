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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RungasJob(val js: JobService, val pjs: PijobService, val ts: TaskService, val gpioService: GpioService,
                val readUtil: ReadUtil, val psij: PortstatusinjobService) {

    @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    fun run() {

        var toruns = loadjob()
        logger.debug("Job to run ${toruns}")
        if (toruns != null && toruns.size > 0) {
            for (j in toruns) {
                if(checktmp(j)) {
                    var task = GasWorker(j, gpioService, readUtil, psij)
                    ts.run(task)
                }

            }
        }

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