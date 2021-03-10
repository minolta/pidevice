package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.CountfgService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.ReadcounterWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
@Profile("!test")
class Readcounter(val pjs: PijobService, val js: JobService, val findJob: FindJob,
                  val logService: LogService, val ts: TaskService, val httpService: HttpService,
                  val cfgs: CountfgService, val iptableServicekt: IptableServicekt) {
    @Scheduled(initialDelay = 3000, fixedDelay = 10000)
    fun read() {
        var jobid: Long? = null
        var mac: String? = null
        try {
            var jobs = findJob.loadjob("readcounter")

            if (jobs != null) {
                for (p in jobs) {
                    mac = p.desdevice?.mac
                    jobid = p.refid
                    if (!ts.checkrun(p)) {
                        var ip = iptableServicekt.findByMac(p.desdevice?.mac!!)
                        if (ip != null) {
                            var t = ReadcounterWorker(cfgs, p, null, ip.ip!!, logService,httpService)
                            ts.run(t)
                        }
                    }

                }
            }
        } catch (e: Exception) {
            logService.createERROR("${e.message}", Date(),
                    "ReadCounter", "", "27", "read()", mac, jobid)
            logger.error("Read Counter error ${e.message}")
        }

    }


    fun readCount(pj: Pijob) {
        var t = Executors.newSingleThreadExecutor()
        try {
            var ip = iptomac(pj.desdevice?.mac!!)
            var url = "http://${ip?.ip}/readcount"
            var http = HttpGetTask(url)
            var f = t.submit(http)

            try {
                var r = f.get(5, TimeUnit.SECONDS)
                println(r)
            } catch (e: Exception) {
                logger.error("Get result job: ${e.message} PI JOB: ${pj.name}")
            }


        } catch (e: Exception) {

        }
    }

    fun iptomac(mac: String): Iptableskt? {
        return iptableServicekt.findByMac(mac)
    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("readcounter")
        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }

        throw Exception("Read count not found")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Readcounter::class.java)
    }

}