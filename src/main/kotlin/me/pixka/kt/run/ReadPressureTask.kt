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
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReadPressureTask(p: Pijob, var mtp:MactoipService,
                       var rps: PressurevalueService, var pideviceService: PideviceService,
                       var ntf: NotifyService) :
        DWK(p),Runnable {
    val om = ObjectMapper()
    var token = System.getProperty("errortoekn")
    override fun run() {
        var p = 0.0
        try {
            startRun = Date()
            status = "Start run : ${Date()}"
            isRun = true
            Thread.currentThread().name = "JOBID:${pijob.id}  ReadPressure:${pijob.name} ${startRun}"
            var ip:String?=null
            try {
                  ip = pijob.desdevice?.ip
            } catch (e: Exception) {
                logger.error("Find device error ${pijob.desdevice?.mac!!} ${e.message}")
            }
            if (ip != null) {

                try {
                    var re = mtp.http.get("http://${ip}",10000)
                    var o = om.readValue<PSIObject>(re)
                    logger.debug("${pijob.name} Get pressure ${o}")
                    status = "${pijob.name} Get pressure ${o}"
                    var pv = PressureValue()
                    try {
                        pv.device = pideviceService.findByMac(pijob.desdevice?.mac!!)
                    } catch (e: Exception) {
                        logger.error("Other device not found ${e.message}")
                    }
                    pv.valuedate = Date()
                    pv.pressurevalue = o.psi
                    if(o.psi!=null)
                    p = o.psi?.toDouble()!!

                    //บอกว่า ให้เก็บค่าเท่าไหร่ ถ้าตำกว่าไม่เก็บ
                    if (pijob.tlow != null) {
                        var tl = pijob.tlow!!.toFloat()
                        var v = o.psi!!.toFloat()
                        if (v < tl) {
                            status = "too low to save value PSI:${v} < ${tl}"
                            isRun=false
                        }
                        else
                        {
                            var p = rps.save(pv)
                        }

                    }
                    else
                    {
                        var d = rps.save(pv)
                    }


                    status = "End ${Date()}"
                } catch (e: Exception) {
                    status = "Error ${e.message}"
                    isRun = false
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            isRun = false
            status = "${pijob.name} error ${e.message}"
        }



        exitdate = findExitdate(pijob)
        status = "${pijob.name} Run Readpressure job  end  PSI:${p}"
        logger.debug("${pijob.name} Run Readpressure job  end ")


    }


    override fun toString(): String {
        return "${pijob.name}"
    }

    var logger = LoggerFactory.getLogger(ReadPressureTask::class.java)
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