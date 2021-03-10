package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.DustWorker
import me.pixka.log.d.LogService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture


@Component
@Profile("!test")
class Rundustjob(val findJob: FindJob, val mtp: MactoipService,
                 val task: TaskService, val ps: PortstatusinjobService,
                 val lgs: LogService) {

    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {

        var jobs = findJob.loadjob("rundustjob")

        if (jobs != null) {
            jobs.forEach {
                CompletableFuture.supplyAsync {
                    if (!task.checkrun(it)) {
                        runThread(it)
                    }
                }

            }
        }
    }

    fun readPm(job: Pijob): Pm? {
        var ip = mtp.mactoip(job.desdevice?.mac!!)
        if (ip != null) {
            val re = mtp.http.get("http://${ip}", 10000)
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
        var it = readPm(job)
        var canrun = checkCanrun(job, it!!)
        if (canrun) {
            var ports = ps.findByPijobid(job.id) as ArrayList<Portstatusinjob>
            var t = DustWorker(job, ports, mtp)
            task.run(t)
        }
    }
}