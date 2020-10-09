package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.VbattService
import me.pixka.kt.pibase.o.VbattObject
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1readvoltWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture

@Component
class ReadVtask(val pjs: PijobService,
                val js: JobService,
                val task: TaskService, val readUtil: ReadUtil,
                val portstatusinjobService: PortstatusinjobService,
                val ips: IptableServicekt,
                val pss: VbattService, val ntf: NotifyService, val findJob: FindJob,
                val httpService: HttpService) {
    var exitdate: Date? = null
    var om = ObjectMapper()
    @Scheduled(fixedDelay = 5000)
    fun run() {
        try {
            var list = findJob.loadjob("runreadvolt")
            if (list != null)
                logger.debug("Job for ReadVtask ${list.size}")

            if (list != null) {
                list.forEach {
                    var id = it.id
                    if (task.runinglist.find { it.getPijobid() == id && it.runStatus() } == null) {
                        var ip = ips.findByMac(it.desdevice?.mac!!)
                        var run = task.run(D1readvoltWorker(it,httpService,pss,ntf,ip!!))
                        logger.debug("Run ? ${run}")
                    }
                    else
                    {
                        logger.debug("Run")
                    }
                }

            }
        } catch (e: Exception) {
            logger.error("ERROR READ V ${e.message}")
        }
    }

//    fun loadjob(): List<Pijob>? {
//        var job = js.findByName("runreadvolt")
//
//        if (job != null) {
//
//            var jobs = pjs.findJob(job.id)
//            return jobs
//        }
//        throw Exception("Not have JOB")
//    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadVtask::class.java)
    }
}