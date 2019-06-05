package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.TimeUtil
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
//@Profile("pi")
class RunoffPump(val pjs: PijobService,
                 val js: JobService,
                 val task: TaskService,
                 val timeUtil: TimeUtil, val dhts: Dhtutil) {


    @Scheduled(fixedDelay = 5000)
    fun run() {

        logger.debug("Run off pump")
        try {
            var jobs = loadjob()
            logger.debug("Found job ${jobs}")
            if (jobs != null) {

                for (job in jobs) {

                    var t = OffpumbWorker(job, timeUtil, dhts)
                    var run = task.run(t)
                    logger.debug("Run ${job} ${run}")
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }

    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("offpump")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunoffPump::class.java)
    }
}

class OffpumbWorker(var pijob: Pijob, val timeUtil: TimeUtil,
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

    fun checktime(): Boolean {
        logger.debug("TIME: ${pijob.lowtime} ${Date().time} ${pijob.hightime}")
        val cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        val gmtTime = cSchedStartCal.time.time
        var inrang = timeUtil.checkInrangbyHighlow(pijob.lowtime!!, gmtTime, pijob.hightime!!)
        logger.debug("In rang ${inrang}")
        return inrang

    }

    override fun run() {

        try {
            if (!checktime()) {
                status = "Not run this time"
                logger.debug("Not run this time")
                isRun = false
                return
            }
        } catch (e: Exception) {
            logger.error("check time: " + e.message)
            status = e.message
            isRun = false
            return
        }

        isRun = true
        startdate = Date()
        status = "Start run ${startdate}"

        if (pijob.desdevice == null) {
            isRun = false
            status = "Not set des device"
            logger.error("Not set des device")
            return
        }

        var ip = dhts.mactoip(pijob.desdevice?.mac!!)

        if (ip == null) {
            isRun = false
            status = "Can not find ip of des device "
            logger.error("Can not find ip of des device ")
            return
        }

        var url = "http://${ip.ip}/status"

        try {
            var ps = call(ip, url)
            if (ps != null) {
                if (ps.status == true) {
                    //pumb is on
                    url = "http://${ip.ip}/off"
                    ps = call(ip, url)
                    if (ps != null)
                        status = "Off result ${ps.status}"
                    else {
                        status = "Can not get off result"
                    }
                }
            }

        } catch (e: Exception) {
            logger.error(e.message)
            status = e.message
            isRun = false
        }

        isRun = false
        status = "End off job"
    }

    fun call(ip: Iptableskt, url: String): PumbStatus? {

        var get = HttpGetTask(url)
        var ee = Executors.newSingleThreadExecutor()
        try {
            var f = ee.submit(get)
            var value = f.get(5, TimeUnit.SECONDS)
            var ps = om.readValue<PumbStatus>(value, PumbStatus::class.java)
            return ps
        } catch (e: Exception) {
            ee.shutdownNow()
            logger.error(e.message)
            throw e
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(OffpumbWorker::class.java)
    }


}

class PumbStatus(var status: Boolean? = false)