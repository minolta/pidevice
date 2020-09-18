package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1tjobWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

@Component
class Runtjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService, val ips: IptableServicekt,
                  val dhs: Dhtutil, val httpService: HttpService, val psij: PortstatusinjobService,
                  val readUtil: ReadUtil, val findJob: FindJob, val readtmp: ReadTmpService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            logger.debug("Start run ${Date()}")
            var list = findJob.loadjob("runtbyd1")
            logger.debug("found job ${list?.size}")
            if (list != null) {
                for (job in list) {
                    try {
                        if (!task.checkrun(job)) {
                            var ip = ips.findByMac(job.desdevice?.mac!!)
                            if (ip != null) {

                                var re = httpService.get("http://${ip.ip}")
                                var t = om.readValue<Tmpobj>(re)
                                if (checktmp(t, job)) {
                                    var testjob = pjs.findByRefid(job.runwithid)
                                    var t = D1tjobWorker(job, readUtil, psij, testjob, ips, httpService)
                                    var run = task.run(t)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("${job.name} ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    fun checktmp(t: Tmpobj, job: Pijob): Boolean {
        var tmp: Double = 0.0

        if (t.tmp != null) {
            tmp = t.tmp?.toDouble()!!
        } else if (t.t != null) {
            tmp = t.t?.toDouble()!!
        }

        if (job.tlow?.toDouble()!! <= tmp && job.thigh?.toDouble()!! >= tmp)
            return true

        return false

    }

    fun readtmp(job: Pijob) {
        CompletableFuture.supplyAsync(Supplier {
            var tmp = readtmp.readTmp(job.desdevice?.ip!!)
            tmp
        }).thenApply {
            var run = checktmp(it, job)
            run
        }.thenApply {
            var run = false
            if (it) {
                var testjob = pjs.findByRefid(job.runwithid)
                var t = D1tjobWorker(job, readUtil, psij, testjob, ips, httpService)
                run = task.run(t)
            }

            if (run)
                println("Run JOB:${job.name}")
            else
                println("Not run ${job.name}")
        }.exceptionally { t -> println("Run error :" + t.message) }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Runtjobbyd1::class.java)
    }
}
