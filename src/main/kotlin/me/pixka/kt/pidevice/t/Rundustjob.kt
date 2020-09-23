package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.DustWorker
import me.pixka.log.d.LogService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture


@Component
class Rundustjob(val findJob: FindJob, val httpService: HttpService, val ips: IptableServicekt,
                 val task: TaskService,val ps:PortstatusinjobService,val lgs:LogService) {

    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {

        var jobs = findJob.loadjob("rundustjob")

        if (jobs != null) {
            jobs.forEach {
                if (!task.checkrun(it)) {
                    runThread(it)
                }
            }
        }
    }

    fun readPm(job: Pijob): Pm? {
        var ip = ips.findByMac(job.desdevice?.mac!!)
        if (ip != null) {
            val re = httpService.get("http://${ip.ip}")
            var dustobj = om.readValue<Pm>(re)
            return dustobj
        }
        return null
    }

    fun checkCanrun(job: Pijob, pm: Pm): Boolean {
        var l = job.tlow
        var h = job.thigh
        var pm25 = pm.pm25

        if (l!!.toDouble() <= pm25!!.toDouble() && pm25.toDouble() <= h!!.toDouble()) {
            return true
        }

        return false

    }

    fun runThread(job: Pijob) {
        CompletableFuture.supplyAsync {
            readPm(job)
        }.thenApply {
            var canrun = false
            if (it != null) {
                canrun = checkCanrun(job, it)
            }
            canrun
        }.thenApply {
            var ports  = ps.findByPijobid(job.id) as ArrayList<Portstatusinjob>
            var t = DustWorker(job, ports,ips,httpService)
            task.run(t)
        }.exceptionally {
            println(it.message)
            false
        }
    }
}