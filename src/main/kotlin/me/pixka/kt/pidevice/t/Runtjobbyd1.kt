package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.D1tjobWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class Runtjobbyd1(
    val pjs: PijobService,
    val js: JobService, val mtp: MactoipService,
    val task: TaskService, val ips: IptableServicekt,
    val dhs: Dhtutil, val httpService: HttpService,
    val psij: PortstatusinjobService,
    val readUtil: ReadUtil, val findJob: FindJob,
    val readtmp: ReadTmpService, val lgs: LogService
) {
    val om = ObjectMapper()
    fun checktmps(tmps: List<Tmpobj>, job: Pijob): Boolean {
        for (i in tmps) {
            if (checktmp(i, job)) {
                return true
            }

        }
        return false
    }

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var mac: String? = null
        var jid: Long? = 0
        try {
            logger.debug("Start run ${Date()}")
            var list = findJob.loadjob("runtbyd1")
            logger.debug("found job ${list?.size}")

            if (list != null) {
                for (job in list) {
                    jid = job.refid
                    try {
                        if (!task.checkrun(job)) {

                            var tmps = readtmp.readTmp(job)
                            if (checktmps(tmps, job)) {
                                var t = D1tjobWorker(job, psij, mtp)
                                var run = task.run(t)
                            }

//                            if (job.desdevice?.mac != null) {
//                                mac = job.desdevice?.mac
//                                var ip = ips.findByMac(job.desdevice?.mac!!)
//                                if (ip != null) {
//
//                                    var re = httpService.get("http://${ip.ip}", 60000)
//                                    var t = om.readValue<Tmpobj>(re)
//                                    if (checktmp(t, job)) {
//                                        var testjob = pjs.findByRefid(job.runwithid)
//                                        var t = D1tjobWorker(job, psij, mtp)
//                                        var run = task.run(t)
//                                    }
//                                }
//                            }
                        }
                    } catch (e: Exception) {
                        logger.error("${job.name} ${e.message}")
                        lgs.createERROR(
                            "${e.message}", Date(), "Runtjobbyd1",
                            "", "37", "run", mac, job.refid
                        )

                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Run t by d1 ${e.message}")
            lgs.createERROR(
                "${e.message}", Date(),
                "Runtjobbyd1", "",
                "30", "run", mac, jid
            )

        }
    }

    fun checktmp(t: Tmpobj, job: Pijob): Boolean {
        try {
            var tmp: Double = 0.0
            if (t.tmp != null) {
                tmp = t.tmp?.toDouble()!!
            } else if (t.t != null) {
                tmp = t.t?.toDouble()!!
            }
            if (job.tlow?.toDouble()!! <= tmp && job.thigh?.toDouble()!! >= tmp)
                return true
        } catch (e: Exception) {
            lgs.createERROR(
                "${e.message}", Date(), "Runtjobbyd1", "",
                "71", "checktmp", job.desdevice?.mac, job.refid
            )
        }
        return false

    }

    fun readtmp(job: Pijob) {
        try {
            var ip = ips.findByMac(job.desdevice?.mac!!)
            if (ip != null) {
                var re = httpService.get("http://${ip}", 15000)
                var tmp = om.readValue<Tmpobj>(re)
                var run = checktmp(tmp, job)
                var testjob = pjs.findByRefid(job.runwithid)
                if (!task.checkrun(job)) {
                    var t = D1tjobWorker(job, psij, mtp)
                    run = task.run(t)
                    if (run)
                        println("Run JOB:${job.name}")
                    else
                        println("Not run ${job.name}")
                }
            }
        } catch (e: Exception) {
            logger.error("Read tmp ${job.name} ERROR ${e.message}")
            lgs.createERROR(
                "${e.message}", Date(),
                "Runtjobbyd1", "", "87", "readtmp",
                job.desdevice?.mac, job.refid
            )
        }
    }

    val logger = LoggerFactory.getLogger(Runtjobbyd1::class.java)

}
