package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.TimeUtil
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
//@Profile("pi")
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
                var t = Executors.newSingleThreadExecutor()
                for (job in jobs) {

                    try {
                        if (checktime(job)) {
                            var ip = dhts.mactoip(job.desdevice?.mac!!)
                            var task = HttpGetTask("http://${ip?.ip}/off")
                            var f = t.submit(task)
                            f.get(2, TimeUnit.SECONDS)
                        }
                    } catch (e: Exception) {
                        logger.error("offpump ${e.message}")
                    }

                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
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

    var df = SimpleDateFormat("HH:mm")
    fun checktime(job: Pijob): Boolean {
        try {
//            df.timeZone = TimeZone.getTimeZone("+0700")
            var n = df.format(Date())

            var now = df.parse(n)
            logger.debug("checktime N:${n} now ${now} now time ${now.time}")
            logger.debug("checktime s: ${job.stimes} ${now} e:${job.etimes}")
            if (job.stimes != null && job.etimes != null) {
                var st = df.parse(job.stimes).time
                var et = df.parse(job.etimes).time
                logger.debug("checktime ${st} <= ${now} <= ${et}")
                if (st <= now.time && now.time <= et)
                    return true
            } else if (job.stimes != null && job.etimes == null) {
                var st = df.parse(job.stimes).time
                logger.debug("checktime ${st} <= ${now} ")
                if (st <= now.time)
                    return true
            } else if (job.stimes == null && job.etimes != null) {
                var st = df.parse(job.etimes).time
                logger.debug("checktime ${st} >= ${now}")
                if (st <= now.time)
                    return true
            } else {
                logger.debug("${job.name} checktime not set ")
                return true
            }
        } catch (e: Exception) {
            logger.error("checktime ${e.message}")
        }

        return false
    }
}


//
//class OnpumbWorker(var pijob: Pijob, val timeUtil: TimeUtil,
//                   val dhts: Dhtutil)
//    : PijobrunInterface, Runnable {
//
//    val om = ObjectMapper()
//    var status: String? = null
//    var isRun = false
//    var startdate: Date? = null
//    override fun setP(pijob: Pijob) {
//        this.pijob = pijob
//    }
//
//    override fun setG(gpios: GpioService) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun runStatus(): Boolean {
//        return isRun
//    }
//
//    override fun getPijobid(): Long {
//        return pijob.id
//    }
//
//    override fun getPJ(): Pijob {
//        return pijob
//    }
//
//    override fun startRun(): Date? {
//        return startdate
//    }
//
//    override fun state(): String? {
//        return status
//    }
//
//    fun checktime(): Boolean {
//        logger.debug("TIME: ${pijob.lowtime} ${Date().time} ${pijob.hightime}")
//        val cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
//        val gmtTime = cSchedStartCal.time.time
//        var inrang = timeUtil.checkInrangbyHighlow(pijob.lowtime!!, gmtTime, pijob.hightime!!)
//        logger.debug("In rang ${inrang}")
//        return inrang
//
//    }
//
//    override fun run() {
//
//        try {
//            if (!checktime()) {
//                status = "Not run this time"
//                logger.debug("Not run this time")
//                isRun = false
//                return
//            }
//        } catch (e: Exception) {
//            logger.error("check time: " + e.message)
//            status = e.message
//            isRun = false
//            return
//        }
//
//        isRun = true
//        startdate = Date()
//        status = "Start run ${startdate}"
//
//        if (pijob.desdevice == null) {
//            isRun = false
//            status = "Not set des device"
//            logger.error("Not set des device")
//            return
//        }
//
//        var ip = dhts.mactoip(pijob.desdevice?.mac!!)
//
//        if (ip == null) {
//            isRun = false
//            status = "Can not find ip of des device "
//            logger.error("Can not find ip of des device ")
//            return
//        }
//
//        var url = "http://${ip.ip}/status"
//
//        try {
//            var ps = call(ip, url)
//            if (ps != null) {
//                if (ps.status == false) {
//                    //pumb is on
//                    url = "http://${ip.ip}/on"
//                    ps = call(ip, url)
//                    if (ps != null)
//                        status = "On result ${ps?.status}"
//                    else {
//                        status = "Can not get On result"
//                    }
//                }
//            }
//
//        } catch (e: Exception) {
//            logger.error(e.message)
//            status = e.message
//            isRun = false
//        }
//
//        isRun = false
//        status = "End On job"
//    }
//
//    fun call(ip: Iptableskt, url: String): PumbStatus? {
//        var get = HttpGetTask(url)
//        var ee = Executors.newSingleThreadExecutor()
//        try {
//            var f = ee.submit(get)
//            var value = f.get(5, TimeUnit.SECONDS)
//            var ps = om.readValue<PumbStatus>(value, PumbStatus::class.java)
//            return ps
//        }catch (e:Exception)
//        {
//            ee.shutdownNow()
//            throw e
//        }
//
//    }
//
//    companion object {
//        internal var logger = LoggerFactory.getLogger(OffpumbWorker::class.java)
//    }
//
//
//}