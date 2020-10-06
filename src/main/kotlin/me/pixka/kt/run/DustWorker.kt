package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*


class DustWorker(var pijob: Pijob, var ports: ArrayList<Portstatusinjob>,
                 var ips: IptableServicekt, val httpService: HttpService, val lgs: LogService) : PijobrunInterface, Runnable {
    var om = ObjectMapper()
    var isRun = false
    var startDate: Date? = null
    var state: String? = null
    var exitdate: Date? = null
    var totalrun = 0
    var totalwait = 0
    override fun setP(pijob: Pijob) {
        TODO("Not yet implemented")
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
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
        return startDate
    }

    override fun state(): String? {
        return state
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }

    fun setEnddate() {
        var t = 0L
        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        t + t + (totalrun + totalwait)
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            isRun = false//ออกเลย
    }

    override fun run() {
        isRun = true
        startDate = Date()
        var mac = ""
        try {
            ports.filter { it.enable == true }.forEach {
                var ip = ips.findByMac(it.device?.mac!!)
                mac = it.device?.mac!!
                if (ip != null) {
                    if (it.runtime != null && it.runtime!!.toInt() > totalrun)
                        totalrun = it.runtime!!.toInt()
                    if (it.waittime != null && it.waittime!!.toInt() > totalwait)
                        totalwait = it.waittime!!.toInt()
                    var url = "http://${ip.ip}/run?port=${it.portname?.name}&delay=${it.runtime}&value=${it.status?.toInt()}&wait=${it.waittime}"
                    var re = httpService.get(url, 10000)
                    var s = om.readValue<Status>(re)
                    state = "Set ${it.portname?.name} to ${it.status?.name} uptime:${s.uptime}  status:${s.status}  Pm2.5:${s.pm25} pm10:${s.pm10} pm1:${s.pm1}"
                }
            }
        } catch (e: Exception) {
            logger.error("${e.message}")
            lgs.createERROR("${e.message}", Date(), "DustWorker", "",
                    "", "run", mac, pijob.refid)
            state = "ERROR ${e.message}"
            isRun = false
        }

        setEnddate()

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DustWorker::class.java)
    }
}