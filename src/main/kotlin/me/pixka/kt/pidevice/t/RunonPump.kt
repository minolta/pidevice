package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.TimeUtil
import me.pixka.kt.run.OffpumpWorker
import me.pixka.kt.run.PijobrunInterface
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class RunonPump(val pjs: PijobService,
                val js: JobService,
                val task: TaskService,
                val timeUtil: TimeUtil, val dhts: Dhtutil) {


    @Scheduled(fixedDelay = 5000)
    fun run() {

        logger.debug("Run off pump")
        try {
            var jobs = loadjob()
            if (jobs != null) {
                logger.debug("Job size ${jobs?.size}")

                var t = Executors.newSingleThreadExecutor()
                for (job in jobs) {
                    var intime = task.checktime(job)
                    try {
                        if (intime) {
                            task.run(OnpumbWorker(job, timeUtil, dhts))
                        } else {
                            logger.debug("Not in time ${job.name}")
                        }
                    } catch (e: Exception) {
                        logger.error("offpump ${job.name} : ${e.message}")
                    }

                }
            }
        } catch (e: Exception) {
            logger.error("On pump ${e.message}")
        }


    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("onpump")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunonPump::class.java)
    }

}


class OnpumbWorker(var pijob: Pijob, val timeUtil: TimeUtil,
                   val dhts: Dhtutil)
    : PijobrunInterface, Runnable {

    val om = ObjectMapper()
    var status: String? = null
    var isRun = false
    var startdate: Date? = null
    override fun setP(pijob: Pijob) {
        this.pijob = pijob
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
        var t = Executors.newSingleThreadExecutor()
        try {
            var ip = dhts.mactoip(pijob.desdevice?.mac!!)
            var task = HttpGetTask("http://${ip?.ip}/on")
            status = "call url http://${ip?.ip}/on"
            var f = t.submit(task)
            try {
                var re = f.get(30, TimeUnit.SECONDS)
                status = "on pumb is ok ${re}"
                TimeUnit.SECONDS.sleep(5)
            } catch (e: Exception) {
                OffpumpWorker.logger.error("on pumb error  onpump ${e.message} ${pijob.name}")
                status = "on pumb error  onpump ${e.message} ${pijob.name}"
                TimeUnit.SECONDS.sleep(5)

            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            status = "offpump ${e.message} ${pijob.name}"
            TimeUnit.SECONDS.sleep(10)

        }
        isRun = false
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OnpumbWorker::class.java)
    }


}