package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.Ds18valueService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import java.util.*

/**
 * use to read ktype or D1
 */
class ReadTmpTask(p: Pijob, readvalue: ReadUtil?, var ips: IptableServicekt,
                  var tmpobj: Tmpobj?, var pideviceService: PideviceService,
                  var httpService: HttpService, val ds18valueService: Ds18valueService) :
        DefaultWorker(p, null, readvalue, null, logger) {
    val om = ObjectMapper()
    var exitdate: Date? = null


    override fun run() {
        var tmp = 0.0
        try {
            startRun = Date()
            status = "Start run : ${Date()}"
            isRun = true
            Thread.currentThread().name = "JOBID:${pijob.id}  Tmp:${pijob.name} ${startRun}"
            if (tmpobj == null) {
                //ไม่มีข้อมูลของ tmpobj
                var ip = ips.findByMac(pijob.desdevice?.mac!!)
                if (ip != null) {
                    var re = httpService.get("http://${ip.ip}")
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
        setEnddate()
        logger.debug("${pijob.name} Run Tmp job  end TMP:${tmp} ")


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
        if (t == 0L || !isRun)
            isRun = false//ออกเลย
    }
    override fun toString(): String {
        return "${pijob.name}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadTmpTask::class.java)
    }
}