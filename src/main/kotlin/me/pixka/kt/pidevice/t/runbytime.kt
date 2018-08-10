package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.Worker
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*


@Component
@Profile("pi", "lite")
class RunjobByTime(val dhts: DhtvalueService, val ts: TaskService
                   , val pjs: PijobService, val js: JobService,
                   val gpios: GpioService, val ms: MessageService, val io: Piio,val ps:PortstatusinjobService) {

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    fun run() {


        logger.info("Start RUN JOB By rumbytime")

        var JOB = js.findByName("RUNBYTIME")

        if (JOB == null) {
            logger.error("Time Job not found rumbytime ")
            return
        }

        var jobs = pjs.findByTime(Date(), JOB.id)
        logger.debug("Found !! rumbytime  ${jobs!!.size}")
        if (jobs != null) {
            logger.debug("Found Time Job rumbytime ${jobs.size}")
            exec(jobs)
        }


        //  sensorService.readDhtvalue()
    }

    fun exec(jobs: List<Pijob>) {

        for (j in jobs) {
            var work = Worker(j, gpios, io,ps) //เปลียนเป็น hwork

            if (ts.run(work)) {
                ms.message("Run rumbytime id : ${work.getPijobid()}", "runjob")
            }
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(RunjobByTime::class.java)
    }
}
