package me.pixka.kt.pidevice.u

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
        try {
            var desid = job.desdevice_id
            var sensorid = job.ds18sensor_id


            var value: DS18value? = null
            try {
                logger.debug("1 Read tmp By pijob ${job} #readtmpbyjob")
                value = ss.readDsOther(desid!!, sensorid)
                logger.debug("2 Read tmp other #readtmpbyjob [${value}]")
                if (value != null) {
                    return value.t
                }
            } catch (e: Exception) {
                logger.error("3 Readother ${e.message}  ")
            }

            var localsensor = dss.find(job.ds18sensor_id)
            logger.debug("4 Found local sensor ? ${localsensor} #readtmpbyjob")

            if (localsensor != null && value == null) {
                logger.debug("5 Read Temp from ${localsensor} ")
                var v = null
                try {
                    var v = io.readDs18(localsensor.name!!)
                } catch (e: Exception) {
                    logger.error("6 ${e.message}")
                }
                value = DS18value()
                value.t = v
                logger.debug("7 Read Temp from ${localsensor}  get ${v} return ${value} #readtmpbyjob")
                if (v != null)
                    return v
            }
            if (value != null) {
                return value.t
            }

        } catch (e: Exception) {
            logger.error("8 ${e.message}")
        }


        logger.error("9 Not found other job for run #readtmpbyjob")
        return null
    }

    /**
     * ใช้สำหรับ อ่านค่า ktype จาก D1
     */
    fun readTfromD1Byjob(job: Pijob): DS18value? {

        //mac to ip
        try {
            var ip = findip(job.desdevice!!.mac!!)
            var url = "http://${ip}/ktype"
            var get = HttpGetTask(url)
            var ee = Executors.newSingleThreadExecutor()
            var f = ee.submit(get)
            var value = f.get(15, TimeUnit.SECONDS)
            if (value != null)
                return Stringtods18value(value)
            else
                throw Exception("Value is null")
        } catch (e: Exception) {
            logger.error("ReadTfromD1Byjob ${e.message}")
            throw e
        }


    }

    fun Stringtods18value(stringvalue: String): DS18value? {
        try {
            logger.debug("70 start convert")
            val v = om.readValue<DS18value>(stringvalue, DS18value::class.java)
            logger.debug("71 Stringtods18value : ${v}")
            logger.debug("==========")
            logger.debug("72 ${v}")
            logger.debug("==========")
            return v
        } catch (e: Exception) {
            logger.error("73 EStringtods18value Error: ${e.message}")
            throw e
        }
    }

    fun findip(mac: String): String? {
        try {
            var ip = ips.findByMac(mac)
            return ip!!.ip

        } catch (e: Exception) {
            logger.error("Find IP ${e.message}")
            throw e
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadUtil::class.java)
    }
}