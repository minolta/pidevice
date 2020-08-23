package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.Checkwaterservice
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.HWorker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

/*
* ใช้สำหรับ หาค่า meทางานด้าน H
* */

@Component
@Profile("pi", "lite")
class RunjobByH(val dhts: DhtvalueService, val ts: TaskService
                , val pjs: PijobService, val js: JobService,
                val gpios: GpioService, val ps: PortstatusinjobService,
                val ms: MessageService, val io: Piio, val cws: Checkwaterservice) {


    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    fun run() {


        logger.info("Start RUN JOB By H")

        var HJOB = js.findByName("H")

        if (HJOB == null) {
            logger.error("H Job not found ")
            return
        }
        var lasth = dhts.last()
        if(lasth == null)
            lasth = io.readDHT()

        logger.debug("Last dhtvalue : ${lasth}")
        if (lasth != null) {
            var jobs = pjs.findByH(lasth.h!!, HJOB.id)
            logger.debug("Found H job ${jobs}")
            if (jobs != null) {
                logger.debug("H jobsize:${jobs.size}")
                exec(jobs)
            }
        }

        //  sensorService.readDhtvalue()
    }

    fun endtime(job: Pijob): Date? {

        try {
            var rt = job.runtime!! * 1000
            //var wt = job.waittime!! * 1000
            var ports = ps.findByPijobid(job.id)
            //job.ports
            var t = 1
            t = ports!!.size

            //rt x t == เวลาการเปิดน้ำทั้งหมด
            var n = Date(Date().time + (rt * t))
            return n
        } catch (e: Exception) {
            logger.error("${e.message}")
        }

        return null
    }

    fun exec(jobs: List<Pijob>) {


        for (j in jobs) {
            var work = HWorker(j, gpios, ms, io, ps) //เปลียนเป็น hwork
            if (ts.checkalreadyrun(work) != null) {
                var et = endtime(j)
                logger.debug("endjobtime -> ${et}")

                if (et != null && cws.can(et)) {
                    ms.message("Run hjobid:${j.refid} ${j}", "run")
                    logger.debug("runhjobid ${j}")
                    ts.run(work)
                } else {
                    logger.error("Some device use water ")
                }
            }
        }
        logger.debug("End exechjob")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunjobByH::class.java)
    }
}