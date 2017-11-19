package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.Worker
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/*
* ใช้สำหรับ หาค่า meทางานด้าน H
* */

@Component
@Profile("pi")
class RunjobByH(val dhts: DhtvalueService, val ts: TaskService
                , val pjs: PijobService, val js: JobService, val gpios: GpioService) {


    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    fun run() {


        logger.info("Start RUN JOB By H")

        var HJOB = js.findByName("H")

        if (HJOB == null) {
            logger.error("H Job not found ")
            return
        }
        var lasth = dhts.last()
        logger.debug("Last dhtvalue : ${lasth}")
        if (lasth != null) {
            var jobs = pjs.findByH(lasth.h!!, HJOB.id)

            if (jobs != null) {
                logger.debug("Found H Job ${jobs.size}")
                exec(jobs)
            }
        }

        //  sensorService.readDhtvalue()
    }

    fun exec(jobs: List<Pijob>) {

        for (j in jobs) {
            var work = Worker(j, gpios)
            ts.run(work)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunjobByH::class.java)
    }
}