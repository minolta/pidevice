package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.Ds18valueService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import java.util.*

/**
 * use to read ktype or D1
 */
class ReadTmpTask(p: Pijob,
                  var tmpobj: Tmpobj?, var pideviceService: PideviceService,
                   val ds18valueService: Ds18valueService,var mtp:MactoipService) :
        DWK(p),Runnable {
    val om = ObjectMapper()


    override fun run() {
        var tmp = 0.0
        try {
            startRun = Date()
            status = "Start run : ${Date()}"
            isRun = true
            Thread.currentThread().name = "JOBID:${pijob.id}  Tmp:${pijob.name} ${startRun}"
            if (tmpobj == null) {
                //ไม่มีข้อมูลของ tmpobj
                var ip =  pijob.desdevice?.ip
                if (ip != null) {
                    var re = mtp.http.get("http://${ip}",2000)
                     tmpobj = om.readValue<Tmpobj>(re)
                }
            }
            var d = DS18value()
            d.t = tmpobj?.tmp
            d.pidevice = pideviceService.findByMac(tmpobj?.mac!!)
            d.valuedate = Date()
            if(d.t!=null)
            tmp = d.t?.toDouble()!!
            ds18valueService.save(d)
        } catch (e: Exception) {
            logger.error(e.message)
            isRun = false
            status = "${pijob.name} error ${e.message}"
        }

//        isRun = false
        exitdate = findExitdate(pijob)
        logger.debug("${pijob.name} Run Tmp job  end TMP:${tmp} ")


    }


    override fun toString(): String {
        return "${pijob.name}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadTmpTask::class.java)
    }
}