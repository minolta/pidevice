package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.Dhtutil
import org.slf4j.LoggerFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OffpumpWorker(var pijob: Pijob, val dhts: Dhtutil) : PijobrunInterface, Runnable {
    var state: String = "init"
    var isRun = false
    var startrun: Date? = null

    var df = SimpleDateFormat("HH:mm")
    override fun setP(p: Pijob) {
        this.pijob = p
    }


    override fun setG(gpios: GpioService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runStatus() = isRun

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return startrun
    }

    override fun state(): String? {
        return state
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }

    override fun run() {
        isRun = true
        startrun = Date()
        state = "Start run ${startrun}"
        logger.debug("Start run ${startrun}")
        var t = Executors.newSingleThreadExecutor()
        try {
            var ip = dhts.mactoip(pijob.desdevice?.mac!!)
            var task = HttpGetTask("http://${ip?.ip}/off")
            state = "call url http://${ip?.ip}/off"
            var f = t.submit(task)
            try {
                var re = f.get(30, TimeUnit.SECONDS)
//                var re = URL("http://${ip?.ip}/off").readText()
                state = "Off pumb is ok ${re}"
                TimeUnit.SECONDS.sleep(5)
            } catch (e: Exception) {
                logger.error("Off pumb error  offpump ${e.message} ${pijob.name}")
                state = "Off pumb error  offpump ${e.message} ${pijob.name}"
                TimeUnit.SECONDS.sleep(5)

            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            state = "offpump ${e.message} ${pijob.name}"
            TimeUnit.SECONDS.sleep(10)

        }
        isRun = false
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OffpumpWorker::class.java)
    }
}