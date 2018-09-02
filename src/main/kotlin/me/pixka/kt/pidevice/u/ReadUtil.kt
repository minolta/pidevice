package me.pixka.kt.pidevice.u

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.s.SensorService
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ReadUtil(val ips: IptableServicekt, val http: HttpControl,
               val dss: DS18sensorService, val io: Piio,
               val pideviceService: PideviceService, val ss: SensorService) {
    val om = ObjectMapper()
    fun readPressureByjob(j: Pijob): PressureValue? {
        var des = j.desdevice
        if (des == null) {
            logger.error("Device not found ${des}")
            return null
        }

        var url = "/pressure"

        var ip = ips.findByMac(des.mac!!)
        if (ip != null) {
            var ipstring = ip.ip
            var re = ""
            try {

                var u = "http://${ipstring}${url}"
                logger.debug("Read pressure ${u}")
                re = http.get(u)
            } catch (e: Exception) {
                logger.error(e.message)
                throw e
            }
            try {
                logger.debug("Pase value")
                var ps = om.readValue<PressureValue>(re, PressureValue::class.java)
                logger.debug("Pressure value ${ps}")
                return ps
            } catch (e: Exception) {
                logger.error(e.message)
                throw  e
            }
        }
        logger.error("Can not find ip")
        throw Exception("Can not find ip")


    }

    /**
     * ใช้สำหรับอ่านข้อมูล ความร้อนโดยที่จะอ่าน local  ถ้าไม่มีให้อ่าน จาก ที่อื่นแทน
     */
    fun readTmpByjob(job: Pijob): BigDecimal? {
        var desid = job.desdevice_id
        var sensorid = job.ds18sensor_id
        var value: DS18value? = null
        logger.debug("Read tmp By pijob ${job} #readtmpbyjob")

        var localsensor = dss.find(job.ds18sensor_id)
        logger.debug("Found local sensor ? ${localsensor}")
        if (localsensor != null) {
            var v = io.readDs18(localsensor.name!!)
            value = DS18value()
            value.t = v
        }

        if (value == null)
            value = ss.readDsOther(desid!!, sensorid!!)


        if (value != null) {
            return value.t
        }




        logger.error("Not found other job for run")
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadUtil::class.java)
    }
}