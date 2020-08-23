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

@Component
@Profile("pi", "lite")
class RunjobByHT(val dhts: DhtvalueService, val ts: TaskService
                 , val pjs: PijobService, val js: JobService,
                 val gpios: GpioService, val ms: MessageService, val io: Piio,
                 val cws: Checkwaterservice, val ps:PortstatusinjobService) {

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    fun run() {


        logger.info("Start RUN JOB By HT HbyT")

        var HJOB = js.findByName("HT")

        if (HJOB == null) {
            logger.error("HT Job not found HbyT ")
            return
        }
        var lasth = dhts.last()
        logger.debug("Last dhtvalue HbyT : ${lasth}")
        if (lasth != null) {
            var jobs = pjs.findByHBytime(lasth.h!!, pjs.datetoLong(Date()), HJOB.id)
            logger.debug("Found !! HbyT  ${jobs!!.size}")
            if (jobs != null) {
                logger.debug("Found H Job HbyT ${jobs.size}")
                exec(jobs)
            }
        }

        //  sensorService.readDhtvalue()
    }

    fun exec(jobs: List<Pijob>) {

        for (j in jobs) {
            var work = HWorker(j, gpios, ms, io,ps) //เปลียนเป็น hwork
            if (ts.checkalreadyrun(work) != null) {
                var et = endtime(j)
                logger.debug("End job time ${et}")
                if (et != null && cws.can(et)) {

                    if (ts.run(work)) {
                        ms.message("Run H by time id : ${j.refid}", "runjob")
                    }
                } else {
                    logger.error("Some device use water ")
                }
            }

        }
    }

    fun endtime(job: Pijob): Date? {
        try {
            var rt = job.runtime!! * 1000
            //var wt = job.waittime!! * 1000
            var ports = ps.findByPijobid(job.id)
                    //job.ports
            var t = 1
            if (ports != null) {
                t = ports.size
            }
            //rt x t == เวลาการเปิดน้ำทั้งหมด
            var n = Date(Date().time + (rt * t))
            return n
        } catch (e: Exception) {
            logger.error("error ${e.message}")
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunjobByHT::class.java)
    }
}
