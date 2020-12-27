package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.ReaddustWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class ReadDust(val findJob: FindJob, val ts: TaskService, val pmService: PmService,
               val httpService: HttpService, val mtp: MactoipService,
               val pideviceService: PideviceService, val lgs: LogService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var jobs = findJob.loadjob("readdust")
        logger.debug("Found job to run ${jobs?.size}")
        var mac: String? = null
        if (jobs != null) {
            jobs.forEach {
                logger.debug("Run READ DUST ${it.name}")
                try {
                    mac = it.desdevice?.mac
                    var i = mtp.mactoip(it.desdevice?.mac!!)
                    if (i != null) {
                        if (!ts.checkrun(it)) {
                            var r = ReaddustWorker(it, i, pmService, pideviceService, mtp)
//                            ts.run(r)
                            logger.debug("Run read dust (${it.name}): " + ts.run(r))
                        }
                    } else {
                        lgs.createERROR("No Ip  ${it.name}", Date(),
                                "ReadDust", "", "39", "run", mac, it.refid)
                        logger.error("Not have ip")
                    }
                } catch (e: Exception) {
                    logger.error("ERROR:" + e.message)
                    lgs.createERROR("ERROR  ${e.message}", Date(), "ReadDust",
                            "", "39", "run", mac, it.refid)

                }
            }
        } else {
            logger.debug("read dust job not found")

        }

    }

    var logger = LoggerFactory.getLogger(ReadDust::class.java)
}

