package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.Dhtutil
import org.slf4j.LoggerFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OffpumpWorker(var pijob: Pijob,
                    val httpService: HttpService,
                    val ips:IptableServicekt) : PijobrunInterface, Runnable {
    var state: String = "init"
    var isRun = false
    var startrun: Date? = null
    var exitdate:Date ?=null
    val om = ObjectMapper()

    var df = SimpleDateFormat("HH:mm")
    override fun setP(p: Pijob) {
        this.pijob = p
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
        logger.debug("Start run ${startrun}")
        try {
            var ip = ips.findByMac(pijob.desdevice?.mac!!)
            try {
                var re = httpService.get("http://${ip?.ip}/off")
                var status = om.readValue<Status>(re)
                state = "Off pumb is ok ${status.uptime} Power is off ok."
            } catch (e: Exception) {
                logger.error("Off pumb error  offpump ${e.message} ${pijob.name}")
                state = "Off pumb error  offpump ${e.message} ${pijob.name}"
                isRun = false
            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            state = "offpump ${e.message} ${pijob.name}"
            isRun = false
        }
        setEnddate()
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OffpumpWorker::class.java)
    }
}