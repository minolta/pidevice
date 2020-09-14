package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import org.slf4j.LoggerFactory
import java.util.*


class DustWorker(var pijob: Pijob, var ports: ArrayList<Portstatusinjob>,
                 var ips: IptableServicekt, val httpService: HttpService) : PijobrunInterface, Runnable {
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
        t += totalrun + totalwait
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            isRun = false//ออกเลย
    }

    override fun run() {
        isRun = true
        startDate = Date()
        try {
            ports.filter { it.enable == true }.forEach {
                var ip = ips.findByMac(it.device?.mac!!)
                if (ip != null) {
                    if (it.runtime != null && it.runtime!!.toInt() > totalrun)
                        totalrun = it.runtime!!.toInt()

                    if (it.waittime != null && it.waittime!!.toInt() > totalwait)
                        totalwait = it.waittime!!.toInt()


                    var url = "http://${ip.ip}/run?port=${it.portname}&delay=${it.runtime}&value=${it.status}&wait=${it.waittime}"
                    var re = httpService.get(url)
                    var s = om.readValue<Status>(re)
                    state = "Set ${it.portname?.name} to ${it.status?.name} uptime:${s.uptime} ok"

                }
            }
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
        }

        setEnddate()

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DustWorker::class.java)
    }
}