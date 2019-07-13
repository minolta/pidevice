package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReadPressureTask(p: Pijob, readvalue: ReadUtil?, var ips: IptableServicekt,
                       var rps: PressurevalueService, var pideviceService: PideviceService) :
        DefaultWorker(p, null, readvalue, null, logger) {
    val om = ObjectMapper()
    override fun run() {
        try {
            startRun = Date()
            status = "Start run : ${Date()}"
            isRun = true
            Thread.currentThread().name = "JOBID:${pijob.id}  ReadPressure:${pijob.name} ${startRun}"
            var ip: Iptableskt? = null
            try {
                ip = ips.findByMac(pijob.desdevice?.mac!!)
            } catch (e: Exception) {
                logger.error("Find device error ${pijob.desdevice?.mac!!} ${e.message}")
            }
            if (ip != null) {
                var ipstring = ip.ip
                var t = Executors.newSingleThreadExecutor()
                var u = "http://${ipstring}/pressure"
                logger.debug("Read pressure from ${u}")
                status = "Read pressure from ${u}"
                var get = HttpGetTask(u)
                var f = t.submit(get)
                try {
                    var re = f.get(30, TimeUnit.SECONDS)
                    var o = om.readValue<PressureValue>(re, PressureValue::class.java)
                    logger.debug("${pijob.name} Get pressure ${o}")
                    status = "${pijob.name} Get pressure ${o}"
                    try {
                        o.device = pideviceService.findByMac(o.device?.mac!!)
                    } catch (e: Exception) {
                        logger.error("Other device not found ${e.message}")
                        o.device = pideviceService.create(o.device?.mac!!, o.device_id!!)
                    }
                    //บอกว่า ให้เก็บค่าเท่าไหร่ ถ้าตำกว่าไม่เก็บ
                    if (pijob.tlow != null) {
                        var tl = pijob.tlow!!.toFloat()
                        var v = o.pressurevalue!!.toFloat()
                        if (v < tl) {
                            isRun = false
                            status = "too low to save value ${v}"
                            return
                        }

                    }
                    var d = rps.save(o)
                    logger.debug("Save Pressure ${d}")
                    status = "Save Pressure ${d}"
                    if (pijob.waittime != null) {
                        status = "Wait time ${pijob.waittime}"
                        TimeUnit.SECONDS.sleep(pijob.waittime!!)
                    }
                    isRun = false
                    status = "End ${Date()}"
                } catch (e: Exception) {
                    status = "Error ${e.message}"
                    isRun = false
                    throw e
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            isRun = false
            status = "${pijob.name} error ${e.message}"
            throw  e
        }

        isRun = false
        logger.debug("${pijob.name} Run Readpressure job  end ")


    }

    override fun toString(): String {
        return "${pijob.name}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadPressureTask::class.java)
    }
}