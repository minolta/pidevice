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
               val ps: PortstatusinjobService, val gpios: GpioService) {

    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Start run DS job #runlocaljob")
        var jobs = loadjob()
        if (jobs != null)
            logger.debug("runds found job for check ${jobs.size}")
        var jobtoruns = check(jobs!!)

        if (jobtoruns.size > 0) {
            logger.debug("#rundsjob Job for run ${jobtoruns.size}")
            for (job in jobtoruns) {
                var w = Worker(job, gpios, io, ps)
                if (ts.checkalreadyrun(w) != null) {
                    var canrun = ts.run(w)

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
            logger.debug("runds check ${job.name} Sensor ${sensor?.name}")
            //logger.debug("#runds Job Sensor ${sensor}")

            if (sensor != null) {

                var tempvalue = io.readDs18(sensor.name!!)
                logger.debug("#runds value Local sensor  ${tempvalue}")
                if (tempvalue != null) {
                    var t = tempvalue?.toFloat()
                    logger.debug("#runds value ===== > ${t}")
                    if (l!! <= t && t <= h!!) {
                        logger.debug("#runds run this job")
                        buf.add(job)

                    } else {
                        logger.error("#runds value not in rang ${l}  < ${t} > ${h}")
                    }
                } else {
                    logger.error("#runds Can not read sensor ${sensor} ")
                }
            }
            else
            {
                logger.error("Sendor not found ${job.name}")
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