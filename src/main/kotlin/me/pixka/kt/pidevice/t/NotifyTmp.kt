package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class NotifyTmp(val js: JobService, val pjs: PijobService,
                val readUtil: ReadUtil, val notifyService: NotifyService) {

    @Scheduled(fixedDelay = 5000)
    fun run() {

        var jobs = loadjob()
        logger.debug("Found  ${jobs}")
        if (jobs != null) {
            for (job in jobs) {
                exe(job)
            }
        }
    }

    fun exe(job: Pijob) {

        if (job.tlow != null && job.thigh != null) {
            var tl = job.tlow?.toDouble()
            var th = job.thigh?.toDouble()

            try {
                var r = readUtil.readTmpByjob(job)

                if (r != null) {
                    var rr = r.toDouble()

                    if (tl!! <= rr && th!! >= rr) {
                        notifyService.message("${job.name} device ${job.desdevice}  [${r}]")
                    }
                }

                if (job.runtime != null) {
                    TimeUnit.SECONDS.sleep(job.runtime!!)
                }

                if(job.waittime!=null)
                {
                    TimeUnit.SECONDS.sleep(job.waittime!!)
                }
            } catch (e: Exception) {
                logger.error(e.message)
            }
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(NotifyTmp::class.java)
    }

    fun loadjob(): List<Pijob>? {
        try {
            var job = js.findByName("notifytmp")
            if (job != null) {
                var jobtorun = pjs.findJob(job.id)
                return jobtorun
            }
            return null
        } catch (e: Exception) {
            RunPressure.logger.error(e.message)
            throw e
        }
    }

}