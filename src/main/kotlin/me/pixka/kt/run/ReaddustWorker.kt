package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.t.ReadDust
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ReaddustWorker(var pijob: Pijob, var ip: String, var service: PmService,
                     var om: ObjectMapper, var pideviceService: PideviceService) : Runnable, PijobrunInterface {
    var isRun = true
    var startrundate = Date()
    var statusmessage = "Create"
    override fun run() {
        try {
            isRun=true
            startrundate  = Date()
            logger.debug("read Start ${startrundate} ${isRun}")
            var ee = Executors.newSingleThreadExecutor()
            var http = HttpGetTask("http://${ip}")
            var call = ee.submit(http)
            var re = call.get()
            logger.debug("Call result : ${re}")
            var pd = om.readValue<Pmdata>(re!!)
            var pid = pideviceService.findByMac(pd.mac!!)
            logger.debug("Pi device is ${pid}")
            var pm = Pm()
            pm.pidevice = pid
            pm.pm1 = pd.pm1
            pm.pm10 = pd.pm10
            pm.pm25 = pd.pm25
            pm.valuedate = Date()
            pm = service.save(pm)
            logger.debug("Save ${pm}")
            if (pijob.runtime != null) {
                TimeUnit.SECONDS.sleep(pijob.runtime!!)
            }
            if (pijob.waittime != null) {
                TimeUnit.SECONDS.sleep(pijob.waittime!!)
            }

        } catch (e: Exception) {
            logger.error(e.message)
        }

        logger.debug("End job ${pijob.name}")
        isRun = false
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
        TODO("Not yet implemented")
    }

    override fun startRun(): Date? {
        return startrundate
    }

    override fun state(): String? {
        return statusmessage
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