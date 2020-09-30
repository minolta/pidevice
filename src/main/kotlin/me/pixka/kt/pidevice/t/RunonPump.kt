package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.Status
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class RunonPump(val pjs: PijobService, val checkTimeService: CheckTimeService,
                val js: JobService, val ips: IptableServicekt,
                val task: TaskService, val findJob: FindJob, val httpService: HttpService,
                val lgs: LogService) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Run on pump")
        try {
            var jobs = findJob.loadjob("onpump")
            if (jobs != null) {
                logger.debug("Job size ${jobs.size}")

                for (job in jobs) {
                    if (checkTimeService.checkTime(job, Date()) && !task.checkrun(job)) {
                        var ip = ips.findByMac(job.desdevice?.mac!!)
                        if (ip != null && !task.run(OnpumbWorker(job, httpService, ip,lgs))) {
//                          logger.error("Can not Run ${job.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(), "RunonPump", "",
                    "", "Run", System.getProperty("mac"))
            logger.error("On pump ${e.message}")
        }


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunonPump::class.java)
    }

}


class OnpumbWorker(var pijob: Pijob, val httpService: HttpService, var ip: Iptableskt?,var lgs:LogService)
    : PijobrunInterface, Runnable {

    val om = ObjectMapper()
    var status: String? = null
    var isRun = true
    var startdate: Date? = null
    var exitdate: Date? = null
    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    fun setEnddate() {
        try {
            var t = 0L

            if (pijob.waittime != null)
                t = pijob.waittime!!
            if (pijob.runtime != null)
                t += pijob.runtime!!
            val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.

            calendar.add(Calendar.SECOND, t.toInt())
            exitdate = calendar.time
            if (t == 0L)
                isRun = false//ออกเลย
        }catch (e:Exception)
        {
            isRun=false
            exitdate = Date()
            lgs.createERROR("${e.message}",Date(),"OnpumbWorker","",
            "","setEnddate()",pijob.desdevice?.mac)
        }
    }

    override fun setG(gpios: GpioService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return startdate
    }

    override fun state(): String? {
        return status
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }


    override fun run() {
        try {
            startdate = Date()
            var ip = ip?.ip
            status = "call url http://${ip}/on"
            try {
                var re = httpService.get("http://${ip}/on",500)
                var s = om.readValue<Status>(re)
                status = "on pumb is ok ${s.status} Uptime ${s.uptime}"
            } catch (e: Exception) {
                logger.error("on pumb error  onpump ${e.message} ${pijob.name}")
                lgs.createERROR("${e.message}",Date(),
                "RunonPump",pijob.name,"","",pijob.desdevice?.mac,pijob.refid)
                status = "on pumb error  onpump ${e.message} ${pijob.name}"
                isRun = false

            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            lgs.createERROR("${e.message}",Date(),
            pijob.name,"","","",pijob.desdevice?.mac,pijob.refid)
            status = "offpump ${e.message} ${pijob.name}"
            isRun = false //ออกเลยที่มีปัญฆา
        }
        setEnddate()
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OnpumbWorker::class.java)
    }


}