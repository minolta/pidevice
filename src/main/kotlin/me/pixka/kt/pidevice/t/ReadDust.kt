package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.ReaddustWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@Component
class ReadDust(val findJob: FindJob, val ts: TaskService, val pmService: PmService,
               val iptableServicekt: IptableServicekt, val pideviceService: PideviceService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {

        var jobs = findJob.loadjob("readdust")
        logger.debug("Found job to run ${jobs?.size}")
        if (jobs != null) {
            jobs.forEach {
                logger.debug("Run READ DUST ${it.name}")
                try {
                    var i = iptableServicekt.findByMac(it.desdevice?.mac!!)
                    if (i != null) {
                        var r = ReaddustWorker(it, i.ip!!, pmService, om, pideviceService)

                        logger.debug("Run read dust (${it.name}): "+ts.run(r))
                    } else {
                        logger.error("Not have ip")
                    }
                } catch (e: Exception) {
                    logger.error("ERROR:"+e.message)
                }
                if(it.runtime!=null)
                    TimeUnit.SECONDS.sleep(it.runtime!!)
                if(it.waittime!=null)
                    TimeUnit.SECONDS.sleep(it.waittime!!)
            }
        } else {
            logger.error("read dust job not found")
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDust::class.java)
    }
}

