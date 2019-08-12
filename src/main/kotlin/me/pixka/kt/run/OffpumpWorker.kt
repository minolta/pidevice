package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.t.RunoffPump
import me.pixka.kt.pidevice.u.Dhtutil
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OffpumpWorker(var pijob: Pijob, val dhts: Dhtutil) : PijobrunInterface, Runnable {
    var state: String = "init"
    var isRun = false
    var startrun: Date? = null

    var df = SimpleDateFormat("HH:mm")
    override fun setP(pijob: Pijob) {
    }

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

    override fun setG(gpios: GpioService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runStatus() = isRun

    override fun getPijobid(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPJ(): Pijob {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startRun(): Date? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun state(): String? {
        return state
    }

    override fun setrun(p: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun run() {

        isRun = true
        startrun = Date()
        state = "Start run ${startrun}"
        var t = Executors.newSingleThreadExecutor()
        var count = 0
        while (true) {
            try {
                if (checktime(pijob)) {
                    count++
                    if (count > 3) {
                        logger.error("Off pumb error time out ${pijob.name}")
                        state = "Off pumb error time out ${pijob.name}"
                        break
                    }
                    var ip = dhts.mactoip(pijob.desdevice?.mac!!)
                    var task = HttpGetTask("http://${ip?.ip}/off")
                    state =  "call url http://${ip?.ip}/off"
                    var f = t.submit(task)
                    try {
                        var re = f.get(30, TimeUnit.SECONDS)
                        state = "call is ok result ${re}"
                        break //if off is ok
                    } catch (e: Exception) {
                        logger.error("Off pumb error  offpump ${e.message} ${pijob.name}")
                        state = "Off pumb error  offpump ${e.message} ${pijob.name}"
                    }
                } else {
                    logger.debug("Out of rang offpump ${pijob.name}")
                    state = "Out of rang offpump ${pijob.name}"
                    break
                }
            } catch (e: Exception) {
                logger.error("offpump ${e.message} ${pijob.name}")
                state = "offpump ${e.message} ${pijob.name}"
                break
            }
        }

        isRun = false
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OffpumpWorker::class.java)
    }
}