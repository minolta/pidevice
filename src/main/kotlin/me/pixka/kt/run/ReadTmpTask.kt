package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.Ds18valueService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * use to read ktype or D1
 */
class ReadTmpTask(p: Pijob, readvalue: ReadUtil?, var ips: IptableServicekt,
                  var rps: Ds18valueService, var pideviceService: PideviceService,
                  var dS18sensorService: DS18sensorService) :
        DefaultWorker(p, null, readvalue, null, logger) {
    val om = ObjectMapper()



    override fun run() {
        try {
            startRun = Date()
            status = "Start run : ${Date()}"
            isRun = true
            Thread.currentThread().name = "JOBID:${pijob.id}  Tmp:${pijob.name} ${startRun}"
            var ip: Iptableskt? = null
            try {
                logger.debug("Des device ${pijob.name} ${pijob.desdevice}")
                ip = ips.findByMac(pijob.desdevice?.mac!!)
            } catch (e: Exception) {
                logger.error("Find device error ${pijob.desdevice?.mac!!} ${e.message}")
                status = "Find device error ${pijob.desdevice?.mac!!} ${e.message}"
            }
            if (ip != null) {
                var ipstring = ip.ip
                var t = Executors.newSingleThreadExecutor()
                var u = "http://${ipstring}/ktype"
                logger.debug("Read tmp from ${u}")
                status = "Read tmp from ${u}"
                var get = HttpGetTask(u)
                var f = t.submit(get)
                try {
                    var re = f.get(30, TimeUnit.SECONDS)
                    var o = om.readValue(re, DS18value::class.java)
                    logger.debug("${pijob.name} Get tmp ${o}")
                    status = "${pijob.name} Get pressure ${o}"
                    try {
                        o.pidevice = pideviceService.findByMac(o.pidevice?.mac!!)
                    } catch (e: Exception) {
                        logger.error("Other device not found ${e.message}")
                        o.pidevice = pideviceService.create(o.pidevice?.mac!!, o.pidevice_id!!)
                    }
                    //บอกว่า ให้เก็บค่าเท่าไหร่ ถ้าตำกว่าไม่เก็บ
                    if (pijob.tlow != null) {
                        var tl = pijob.tlow!!.toFloat()
                        var v = o.t!!.toFloat()
                        if (v < tl) {
                            isRun = false
                            status = "too low to save value ${v}"
                            return
                        }

                    }
                    var sensor = dS18sensorService.findorcreate(o.pidevice?.mac!!)
                    o.ds18sensor = sensor
                    o.ds18sensor_id = sensor?.id
                    o.valuedate=Date()
                    o.adddate=Date()
                    var d = rps.save(o)
                    logger.debug("Save Tmp ${d}")
                    status = "Save Tmp  ${d}"
                    if (pijob.runtime != null) {
                        status = "RUN time ${pijob.runtime}"
                        TimeUnit.SECONDS.sleep(pijob.runtime!!)
                    }
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
        logger.debug("${pijob.name} Run Tmp job  end ")


    }

    override fun toString(): String {
        return "${pijob.name}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadTmpTask::class.java)
    }
}