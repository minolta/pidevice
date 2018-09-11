package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.Worker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Rundsjob(val pjs: PijobService, val js: JobService, val io: Piio, val ts: TaskService,
               val ps: PortstatusinjobService, val gpios: GpioService ) {

    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Start run DS job #runlocaljob")
        var jobs = loadjob()
        var jobtoruns = check(jobs!!)

        if (jobtoruns.size > 0) {
            for (job in jobtoruns) {
                var w = Worker(job, gpios, io, ps)
                if(ts.checkalreadyrun(w)!=null)
                {
                    ts.run(w)
                }
            }
        }

            logger.debug("End local ds job")

    }

    fun check(jobs: List<Pijob>): ArrayList<Pijob> {
        logger.debug("Check DS job to run  ${jobs}")
        var buf = ArrayList<Pijob>()
        for (job in jobs) {

            var l = job.tlow?.toFloat()
            var h = job.thigh?.toFloat()


            var sensor = job.ds18sensor


            if (sensor != null) {

                var tempvalue = io.readDs18(sensor.name!!)
                if (tempvalue != null) {
                    var t = tempvalue?.toFloat()
                    if (l!! <= t && t <= h!!) {
                        buf.add(job)
                    }
                }
            }


        }

        logger.debug("End check job ${buf} ")
        return buf
    }

    fun loadjob(): List<Pijob>? {
        var DSJOB = js.findByName("DS")
        var jobs = pjs.findJob(DSJOB.id)
        return jobs
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Rundsjob::class.java)
    }

}