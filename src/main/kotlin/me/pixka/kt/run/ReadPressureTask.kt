package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.o.PSIObject
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReadPressureTask(p: Pijob, var ips: IptableServicekt,val httpService: HttpService,
                       var rps: PressurevalueService, var pideviceService: PideviceService,
                       var ntf: NotifyService) :
        DefaultWorker(p, null, null, null, logger) {
    val om = ObjectMapper()
    var token = System.getProperty("errortoekn")
    var exitdate:Date?=null
    fun setEnddate() {
        var t = 0L
        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            isRun = false//ออกเลย
    }
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
//                var t = Executors.newSingleThreadExecutor()
//                var u = "http://${ipstring}/pressure"
//                logger.debug("Read pressure from ${u}")
//                status = "Read pressure from ${u}"
//                var get = HttpGetTask(u)
//                var f = t.submit(get)
                try {
//                    var re = URL(u).readText()
                    var re = httpService.get("http://${ipstring}")
//                    var re = f.get(30, TimeUnit.SECONDS)
                    var o = om.readValue<PSIObject>(re)
                    logger.debug("${pijob.name} Get pressure ${o}")
                    status = "${pijob.name} Get pressure ${o}"
                    var pv = PressureValue()
                    try {
                        pv.device = pideviceService.findByMac(o.mac!!)
                    } catch (e: Exception) {
                        logger.error("Other device not found ${e.message}")
                        pv.device = pideviceService.create(ip.mac!!,ip.mac!!)
                    }
                    pv.valuedate = Date()
                    pv.pressurevalue = o.psi

//                    if(!o.errormessage.isNullOrEmpty())
//                    {
//                        ntf.message("Have error ${o.errormessage}",token)
//                    }
                    //บอกว่า ให้เก็บค่าเท่าไหร่ ถ้าตำกว่าไม่เก็บ
                    if (pijob.tlow != null) {
                        var tl = pijob.tlow!!.toFloat()
                        var v = o.psi!!.toFloat()
                        if (v < tl) {
//                            isRun = false
                            status = "too low to save value ${v}"

                        }

                    }
                    else
                    {
                        var d = rps.save(pv)
                    }


//                    logger.debug("Save Pressure ${d}")
//                    status = "Save Pressure ${d}"
//                    if (pijob.waittime != null) {
//                        status = "Wait time ${pijob.waittime}"
//                        TimeUnit.SECONDS.sleep(pijob.waittime!!)
//                    }
//                    isRun = false
                    status = "End ${Date()}"
                } catch (e: Exception) {
                    status = "Error ${e.message}"
                    isRun = false
//                    throw e
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            isRun = false
            status = "${pijob.name} error ${e.message}"
//            throw  e
        }

//        isRun = false

        setEnddate()
        status = "${pijob.name} Run Readpressure job  end "
        logger.debug("${pijob.name} Run Readpressure job  end ")


    }

    override fun toString(): String {
        return "${pijob.name}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadPressureTask::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ReadPressure(var device_id: Long? = null, var errormessage: String? = null,
                   var device: PiDevice? = null, var pressurevalue: BigDecimal? = null) {
    fun toPressureValue(): PressureValue {
        var p = PressureValue()
        p.device = device
        if (device != null)
            p.device_id = device!!.id
        p.pressurevalue = pressurevalue
        return p
    }
}