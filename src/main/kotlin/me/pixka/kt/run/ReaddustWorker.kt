package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*


class ReaddustWorker(job: Pijob, var ip: String, var service: PmService, var pideviceService: PideviceService,
                     val mtp: MactoipService)
    : Runnable, DWK(job) {

    override fun run() {
        var pm = Pm()
        var mac: String? = null
        try {
            isRun = true
            startRun = Date()
            logger.debug("read Start ${startRun} ${isRun}")
            var re = mtp.http.get("http://${ip}", 12000)
            var pd = mtp.om.readValue<Pmdata>(re)
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
            mtp.lgs.createERROR("ERROR READ DUST ${e.message}", Date(),
                    "ReaddustWorker", "",
                    "", "run", mac)
            status = "${e.message}"
            exitdate = Date()
            isRun = false
            throw e
        }
        exitdate = findExitdate(pijob)
        logger.debug("End job ${pijob.name}")
        status = "End job PM1:${pm.pm1} PM2.5:${pm.pm25} PM10 ${pm.pm10}"
    }

    var logger = LoggerFactory.getLogger(ReaddustWorker::class.java)
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Pmdata(var pm1: BigDecimal? = null, var pm25: BigDecimal? = null, var pm10: BigDecimal? = null, var mac: String? = null) {
    override fun toString(): String {
        return "PM1 : ${pm1} PM2.5:${pm25} PM10:${pm10} MAC:${mac}"
    }
}