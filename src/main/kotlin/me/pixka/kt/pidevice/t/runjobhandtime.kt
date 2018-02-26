package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.HWorker
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("pi")
class RunjobByHT(val dhts: DhtvalueService, val ts: TaskService
                 , val pjs: PijobService, val js: JobService,
                 val gpios: GpioService) {

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    fun run() {


        RunjobByH.logger.info("Start RUN JOB By HT HbyT")

        var HJOB = js.findByName("HT")

        if (HJOB == null) {
            RunjobByH.logger.error("HT Job not found HbyT ")
            return
        }
        var lasth = dhts.last()
        RunjobByH.logger.debug("Last dhtvalue HbyT : ${lasth}")
        if (lasth != null) {
            var jobs = pjs.findByHBytime(lasth.h!!, pjs.datetoLong(Date()), HJOB.id)
            logger.debug("Found !! HbyT  ${jobs!!.size}")
            if (jobs != null) {
                RunjobByH.logger.debug("Found H Job HbyT ${jobs.size}")
                exec(jobs)
            }
        }

        //  sensorService.readDhtvalue()
    }

    fun exec(jobs: List<Pijob>) {

        for (j in jobs) {
            var work = HWorker(j, gpios) //เปลียนเป็น hwork
            ts.run(work)
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(RunjobByHT::class.java)
    }
}
