package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Vbatt
import me.pixka.kt.pibase.d.VbattService
import me.pixka.kt.pibase.o.VbattObject
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

class D1readvoltWorker(var pijob: Pijob, val httpService: HttpService,
                       val pss: VbattService,
                       var ntf: NotifyService, val ip: Iptableskt) : Runnable, PijobrunInterface {
    val om = ObjectMapper()
    var isRun = false
    var startRun: Date? = null
    var status: String = ""
    var exitdate: Date? = null
    val token = System.getProperty("errortoken")
    fun findExitdate(pijob: Pijob): Date? {
        var t = 0L

        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.

        calendar.add(Calendar.SECOND, t.toInt())
        var exitdate = calendar.time
        if (t == 0L)
            return null

        return exitdate
    }

    override fun run() {
        isRun = true
        startRun = Date()
        Thread.currentThread().name = "JOBID:${pijob.id} D1readvolt : ${pijob.name} ${startRun}"
        try {
            var re = httpService.get("http://${ip.ip}",500)
            var v = om.readValue<VbattObject>(re)
            var psv = Vbatt()
            psv.pidevice_id = pijob.desdevice_id
            psv.pidevice = pijob.desdevice
            psv.valuedate = Date()
            psv.v = v.batt_volt
            pss.save(psv)
            status = "Wait ${pijob.waittime?.toLong()}"
            exitdate = findExitdate(pijob)
        } catch (e: Exception) {
            isRun = false
            logger.error(e.message)
            status = e.message.toString()
            throw e
        }

//        isRun = false
        status = "End job and wait"

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(D1readvoltWorker::class.java)
    }

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
        return startRun
    }

    override fun state(): String? {
        return status
    }

    override fun setrun(p: Boolean) {

        isRun = p
    }


}

@JsonIgnoreProperties(ignoreUnknown = true)
class Espstatus(var batt_volt: BigDecimal? = null, var errormessage: String? = null)