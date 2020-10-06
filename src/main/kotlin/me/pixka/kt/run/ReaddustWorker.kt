package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors


class ReaddustWorker(var pijob: Pijob, var ip: String, var service: PmService,val httpService: HttpService,
                     var om: ObjectMapper, var pideviceService: PideviceService,val lgs:LogService)
    : Runnable, PijobrunInterface {
    var isRun = true
    var startrundate = Date()
    var statusmessage = "Create"
    var exitdate: Date? = null
    var state:String? = ""
    fun setEnddate() {
        var t = 0L

        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.

        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L || !isRun)
            isRun = false//ออกเลย
    }

    override fun run() {
        var pm = Pm()
        var mac:String?=null
        try {
            isRun = true
            startrundate = Date()
            logger.debug("read Start ${startrundate} ${isRun}")
            var re = httpService.get("http://${ip}",12000)
            var pd = om.readValue<Pmdata>(re)
            var pid = pideviceService.findByMac(pd.mac!!)
            mac = pd.mac
            logger.debug("Pi device is ${pid}")
            pm.pidevice = pid
            pm.pm1 = pd.pm1
            pm.pm10 = pd.pm10
            pm.pm25 = pd.pm25
            pm.valuedate = Date()
            pm = service.save(pm)
            logger.debug("Save ${pm}")
        } catch (e: Exception) {
            logger.error(e.message)
            lgs.createERROR("ERROR READ DUST ${e.message}",Date(),
                    "ReaddustWorker","",
            "","run",mac)
            state = e.message
            exitdate = Date()
            isRun=false
            throw e
        }
        setEnddate()
        logger.debug("End job ${pijob.name}")
        state = "End job PM1:${pm.pm1} PM2.5:${pm.pm25} PM10 ${pm.pm10}"
    }

    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {

        return this.pijob.id
    }

    override fun getPJ(): Pijob {

        return pijob
    }

    override fun startRun(): Date? {
        return startrundate
    }

    override fun state(): String? {
        return state
    }

    override fun setrun(p: Boolean) {
        this.isRun = p
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReaddustWorker::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Pmdata(var pm1: BigDecimal? = null, var pm25: BigDecimal? = null, var pm10: BigDecimal? = null, var mac: String? = null) {
    override fun toString(): String {
        return "PM1 : ${pm1} PM2.5:${pm25} PM10:${pm10} MAC:${mac}"
    }
}