package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1TimerWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class runD1Timer(val pjs: PijobService,
                 val js: JobService, val findJob: FindJob,
                 val task: TaskService, val ips: IptableServicekt,
                 val line: NotifyService,
                 val dhs: Dhtutil, val httpService: HttpService, val psij: PortstatusinjobService,
                 val readUtil: ReadUtil) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {

        try {
            var list = findJob.loadjob("runtimerbyd1")
            if (list != null)
                logger.debug("Job for Runhjobbyd1 ${list.size}")
            if (list != null) {
                for (job in list) {
                    try {
                        logger.debug("Run ${job}")
                        var ip = ips.findByMac(job.desdevice?.mac!!)
                        if (ip != null) {
                            var re = httpService.get("http://${ip.ip}")
                            var tmpobj = om.readValue<Tmpobj>(re)
                            if (job.tlow?.toDouble()!! <= tmpobj.tmp?.toDouble()!!) {
                                //ถ้า tmp == tlow จะทำการ รอ thigh
                                var t = D1TimerWorker(job, ips, readUtil, psij, null, line,httpService)
                                var run = task.run(t)
                                logger.debug("RunJOB ${run}")
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Error ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error ${e.message}")

        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(runD1Timer::class.java)
    }
}