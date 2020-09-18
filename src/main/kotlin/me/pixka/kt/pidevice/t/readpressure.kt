package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pibase.o.PSIObject
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.ReadPressureTask
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
//@Profile("pi")
class Findreadpressure(val pideviceService: PideviceService, val ps: PressurevalueService,
                       val js: JobService, val pjs: PijobService, val findJob: FindJob,
                       val httpService: HttpService, val ips: IptableServicekt, val ts: TaskService,
                       val ntf: NotifyService
) {

    var om = ObjectMapper()

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun find() {
        // testread()
        logger.info("Start read Pressure")
        var jobtorun = findJob.loadjob("readpressure")
        try {
            if (jobtorun != null) {
                logger.debug("Found job  ${jobtorun.size}")
                for (j in jobtorun) {
                    if (!ts.checkrun(j)) {
                        var t = ReadPressureTask(j, ips, httpService, ps, pideviceService, ntf)
                        var run = ts.run(t)
                        logger.debug("Run ${j.name} == ${run}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("4 ${e.message}")
            //throw e
        }
        logger.debug(" 5 End Task read pressure")
    }






    companion object {
        internal var logger = LoggerFactory.getLogger(Findreadpressure::class.java)
    }

}
