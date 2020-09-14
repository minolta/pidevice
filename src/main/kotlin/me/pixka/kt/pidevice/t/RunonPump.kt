package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.TimeUtil
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.Status
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Component
class RunonPump(val pjs: PijobService,val checkTimeService: CheckTimeService,
                val js: JobService,
                val task: TaskService, val findJob: FindJob,val httpService: HttpService,
                val timeUtil: TimeUtil, val dhts: Dhtutil) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Run on pump")
        try {
            var jobs = findJob.loadjob("onpump")
            if (jobs != null) {
                logger.debug("Job size ${jobs.size}")

                for (job in jobs) {
                  if(checkTimeService.checkTime(job,Date()) && !task.checkrun(job))
                  {
                      if(!task.run(OnpumbWorker(job, httpService, dhts)))
                      {
                          logger.error("Can not Run ${job.name}")
                      }
                  }
//                    try {
//                        if (intime && task.runinglist.find { it.getPijobid()==job.id && it.runStatus()==true }==null) {
//                            var run = task.run(OnpumbWorker(job, httpService, dhts))
//                            logger.debug("Can run ${run}")
//                        } else {
//                            logger.debug("Not in time ${job.name}")
//                        }
//                    } catch (e: Exception) {
//                        logger.error("Onpump ${job.name} : ${e.message}")
//                    }

                }
            }
        } catch (e: Exception) {
            logger.error("On pump ${e.message}")
        }


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunonPump::class.java)
    }

}


class OnpumbWorker(var pijob: Pijob, val httpService: HttpService,
                   val dhts: Dhtutil)
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
            var ip = dhts.mactoip(pijob.desdevice?.mac!!)
            status = "call url http://${ip?.ip}/on"
            try {
                var re = httpService.get("http://${ip?.ip}/on")
                var s = om.readValue<Status>(re)
                status = "on pumb is ok ${s.status} Uptime ${s.uptime}"
            } catch (e: Exception) {
                logger.error("on pumb error  onpump ${e.message} ${pijob.name}")
                status = "on pumb error  onpump ${e.message} ${pijob.name}"
                isRun = false

            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            status = "offpump ${e.message} ${pijob.name}"
        }
//        isRun = false
        setEnddate()
//        status = "End job "
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OnpumbWorker::class.java)
    }


}