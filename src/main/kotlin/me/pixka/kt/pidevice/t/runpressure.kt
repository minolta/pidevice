package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.Worker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi", "lite")
class RunPressure(val reader: ReadUtil, val js: JobService, val pjs: PijobService,
                  val io: Piio, val gpio: GpioService, val ps: PortstatusinjobService,
                  val ts: TaskService) {
    @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    fun run() {

        var toruns = loadjob()
        logger.debug("Job to run ${toruns}")
        if (toruns != null && toruns.size > 0) {
            for (j in toruns) {
                var task = check(j)
                if (task != null) {

                    var t = ts.checkalreadyrun(task)
                    logger.debug("Check job  ${task}  can ${t}")
                    if (t != null) {
                        ts.run(t)
                    }
                }
            }
        }

    }

    fun check(j: Pijob): Worker? {
        var p = reader.readPressureByjob(j)
        logger.debug("Pressure Value ${p}")
        if (p != null) {
            var pl = j.tlow?.toFloat()
            var ph = j.thigh?.toFloat()
            var pv = p.pressurevalue?.toFloat()

            logger.debug("Value Low ${pl} Value ${pv} High ${ph}")
            if (pv!! >= pl!! && pv <= ph!!) {
                return Worker(j, gpio, io, ps)
            } else {
                logger.warn("ERROR pressure not in rang")
            }

        }
        return null
    }


    fun loadjob(): List<Pijob>? {
        try {
            var job = js.findByName("runpressure")
            if (job != null) {
                var jobtorun = pjs.findJob(job.id)
                return jobtorun
            }
            return null
        }catch (e:Exception)
        {
            logger.error(e.message)
            throw e
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunPressure::class.java)
    }
}