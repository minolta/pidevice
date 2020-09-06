package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.ReadPressureTask
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
//@Profile("pi")
class Findreadpressure(val pideviceService: PideviceService, val ps: PressurevalueService,
                       val js: JobService, val pjs: PijobService,val findJob: FindJob,
                       val http: HttpControl, val ips: IptableServicekt, val ts: TaskService, val ntf: NotifyService
) {


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun find() {

        // testread()
        logger.info("Start read Pressure")
        var jobtorun = loadJob()
        try {
            if (jobtorun != null) {
                logger.debug("Found job  ${jobtorun.size}")
                for (j in jobtorun) {

                    var t = ReadPressureTask(j,null,ips,ps,pideviceService,ntf)
                    ts.run(t)
                }
            }
        } catch (e: Exception) {
            logger.error("4 ${e.message}")
            //throw e
        }

        logger.debug(" 5 End Task read pressure")
    }

    fun loadJob(): List<Pijob>? {
        try {
            var job = js.findByName("readpressure")

            if (job != null) {
                var jobtorun = pjs.findJob(job.id)
                return jobtorun
            }
        } catch (e: Exception) {
            logger.error("Loadjob: ${e.message}")
            throw e
        }

        return null

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Findreadpressure::class.java)
    }

}
