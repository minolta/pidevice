package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.CoundownWorkerii
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal


@Component
@Profile("pi")
class Runcooldown(val js: JobService, val pjs: PijobService,
                  val gpios: GpioService, val ts: TaskService, val ss: SensorService,
                  val m: MessageService, val i: Piio, val ppp: PortstatusinjobService) {

    @Scheduled(initialDelay = 2000, fixedDelay = 30000)
    fun run() {

        logger.info("Run Cooldown")

        var job = js.findByName("cooldown") //สำหรับแสดงผล

        if (job == null) {
            logger.error("JOB COOL DOWN NOT FOUND")
            return
        }

        var jobs = pjs.findJob(job.id)


        if (jobs != null) {
            logger.debug("Found Jobs ${jobs.size}")
            exe(jobs)


        } else
            logger.debug("Job not found ")

    }

    fun exe(jobs: List<Pijob>) {

        for (pj in jobs) {
            var checkvar = readDs(pj)
            if (checkvar == null || checkvar.compareTo(pj.tlow) < 0) {
                logger.error("Not in ranger job not start ${pj}")
                continue
            } else {
                logger.debug("Cool job can run")

                var c = CoundownWorkerii(pj,gpios,ss)
                ts.run(c)
            }
        }
    }

    fun readDs(pijob: Pijob): BigDecimal? {

        var desid = pijob.desdevice_id
        var sensorid = pijob.ds18sensor_id

        var value = i.readDs18(pijob.ds18sensor?.name!!)
        if (value == null) {
            var dsvalue = ss.readDsOther(desid!!, sensorid!!)
            if (dsvalue != null) {
                value = dsvalue.t
            }
        }

        return value
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runcooldown::class.java)
    }
}