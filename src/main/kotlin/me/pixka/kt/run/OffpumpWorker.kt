package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

class OffpumpWorker(var pijob: Pijob,
                    val httpService: HttpService,
                    val ips: IptableServicekt,
                    val lgs: LogService) : PijobrunInterface, Runnable {
    var state: String = "init"
    var isRun = false
    var startrun: Date? = null
    var exitdate: Date? = null
    val om = ObjectMapper()

    var df = SimpleDateFormat("HH:mm")
    override fun setP(p: Pijob) {
        this.pijob = p
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
        }
        catch (e:Exception)
        {
            isRun = false
            logger.error(e.message)
        }
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

    override fun exitdate(): Date? {
        return exitdate
    }

    override fun run() {
        isRun = true
        startrun = Date()
        logger.debug("Start run ${startrun}")
        var mac: String? = null
        try {
            var ip = ips.findByMac(pijob.desdevice?.mac!!)
            mac = pijob.desdevice?.mac
            try {
                var re = httpService.get("http://${ip?.ip}/off",12000)
                var status = om.readValue<Status>(re)
                state = "Off pumb is ok ${status.uptime} Power is off ok."
            } catch (e: Exception) {
                logger.error("Off pumb error  offpump ${e.message} ${pijob.name}")
                lgs.createERROR(" ${e.message}", Date(),
                        "OffpumpWorker", "", "", "run", mac, pijob.refid)
                state = "Off pumb error  offpump ${e.message} ${pijob.name}"
                isRun = false
            }
        } catch (e: Exception) {
            logger.error("offpump ${e.message} ${pijob.name}")
            lgs.createERROR("${e.message}", Date(), "OffpumpWorker",
                    "", "", "run", mac, pijob.refid)
            state = "offpump ${e.message} ${pijob.name}"
            isRun = false
        }
        setEnddate()
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(OffpumpWorker::class.java)
    }
}