package me.pixka.kt.pidevice.u

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.InfoService
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class ReadUtil(val ips: IptableServicekt, val http: HttpControl, val iptableServicekt: IptableServicekt,
               val dss: DS18sensorService, val io: Piio, val ps: PideviceService,
               val pideviceService: PideviceService, val ss: SensorService, val infoService: InfoService) {
    val om = ObjectMapper()

    var buffer = ArrayList<ReadBuffer>()

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

    fun readOther(desid: Long, senid: Long? = null): DS18value? {


        var url: String? = null

        try {
            url = findurl(desid, senid)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
        try {
            var get = HttpGetTask(url!!)
            var ee = Executors.newSingleThreadExecutor()
            var f = ee.submit(get)
            var value = f.get(15, TimeUnit.SECONDS)
            if (value != null) {
                return Stringtods18value(value)
            }
            throw Exception("Not found Ds18value ")

        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }


    }

    fun findDevice(desid: Long): PiDevice {
        try {
            var desdevice = ps.find(desid) //เปลียน ip แล้ว
            //var desdevice = ps.findByRefid(desid) //refid จะ save ตอน load pijob
            logger.debug("1 Find pidevice ${desid} found ===> ${desdevice} #readother")
            if (desdevice == null)
                throw Exception("Device no found")
            return desdevice
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun findIp(desdevice: PiDevice): Iptableskt? {

        var ip = iptableServicekt.findByMac(desdevice.mac!!)
        logger.debug("3 Find ip of pidevice ${desdevice} found ===> ${ip} #readother")
        if (ip == null || ip.ip == null) {
            logger.error("4 Can not find ip ${ip} #readother")
            throw Exception("Ip not found")
        }
        return ip

    }

    fun findurl(desid: Long, sensorid: Long?): String? {

        var desdevice: PiDevice? = null
        try {
            desdevice = findDevice(desid)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }

        var sensor: DS18sensor? = null
        try {
            sensor = dss.find(sensorid)
        } catch (e: Exception) {
            logger.error("Find Sensor Error")

        }
        logger.debug("2 Find sensor ${sensorid} found ===> ${sensor} #readother")

        var ip: Iptableskt? = null
        try {
            ip = findIp(desdevice)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
        var url = ""

        if (ip != null && ip.ip != null) {

            if (sensor == null) {
                url = "http://${ip.ip}/ktype"
            } else
                if (sensor.name != null && sensor.name?.indexOf("28-") != -1)
                    url = "http://${ip.ip}/ds18valuebysensor/${sensor.name}"
                else {
                    //เป็น ktype จะไม่มี 28- ให้อานตรงๆเลย
                    url = "http://${ip.ip}/ktype"
                }
        }
        logger.debug("6 Read URL: [${url}] #readother")
        return url

    }

    /**
     * ใช้สำหรับตรวจสอบ pijob ว่า แรงดัน อยู่ในช่วนหรือเปล่า
     */
    fun checkLocalPressure(pijob: Pijob): Boolean {
        try {
            logger.debug("Run localpressure")
            var now = infoService.A0?.psi?.toDouble()
            if (now == null)
                throw Exception("Can not read localpressure")

            logger.debug("Read local localpressure ${now}")
            if (pijob.hlow != null && pijob.hhigh != null) {

                var l = pijob.hlow?.toDouble()
                var h = pijob.hhigh?.toDouble()

                logger.debug("check localpressure l:${l} <= ${now}  <= ${h}")
                if (l!! <= now && now <= h!!) {
                    logger.debug("In rang localpressure")
                    return true
                }
                logger.debug(" pressure in  rang localpressure ")
                return false
            }

        } catch (e: Exception) {
            logger.error("${e.message} localpressure")
            throw e
        }
        throw Exception("Pressure not set localpressure")

    }

    fun readLocal(job: Pijob): DS18value? {
        var localsensor = dss.find(job.ds18sensor_id)
        logger.debug("4 Found local sensor ? ${localsensor} #readtmpbyjob")
        var value: DS18value? = null
        if (localsensor != null && value == null) {
            logger.debug("5 Read Temp from ${localsensor} ")
            var v = null
            try {
                var v = io.readDs18(localsensor.name!!)
            } catch (e: Exception) {
                logger.error("6 ${e.message}")
                throw e
            }
            value = DS18value()
            value.t = v
            logger.debug("7 Read Temp from ${localsensor}  get ${v} return ${value} #readtmpbyjob")
            if (v != null)
                return value
        }
        if (value != null) {
            return value
        }
        throw Exception("Read local not found")

    }

    fun checktmp(p: Pijob): Boolean {

        try {
            var tmp = readTmpByjob(p)
            if (tmp != null) {
                var tl = p.tlow?.toFloat()
                var th = p.thigh?.toFloat()
                var v = tmp.toFloat()

                if (tl != null) {
                    if (tl <= v && v <= th!!) {
                        return true
                    }
                }

                return false

            }
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
        return false
    }

    /**
     * ใช้สำหรับอ่านข้อมูล ความร้อนโดยที่จะอ่าน local  ถ้าไม่มีให้อ่าน จาก ที่อื่นแทน
     */
    fun readTmpByjob(job: Pijob): BigDecimal? {
        try {
            var desid = job.desdevice_id
            var sensorid = job.ds18sensor_id


            var value: DS18value? = null
            if (desid != null) {
                try {
                    value = readOther(desid, sensorid)
                    if (value != null) {
                        savebuffer(job, value.t!!)
                        return value.t
                    }
                } catch (e: Exception) {
                    logger.error(e.message)

                }

                try {
                    var p = readBuffer(job)
                    return p
                } catch (e: Exception) {
                    logger.error(e.message)
                    throw e
                }
            }


            try {
                value = readLocal(job)
                if (value != null) {

                    savebuffer(job, value.t!!)
                    return value.t
                }
            } catch (e: Exception) {
                logger.error(e.message)
                try {
                    var p = readBuffer(job)
                    return p
                } catch (e: Exception) {
                    logger.error(e.message)
                    throw e
                }
            }


        } catch (e: Exception) {
            logger.error("8 ${e.message}")
            throw e
        }

        throw Exception("Not found readTmpByjob")


    }

    fun readBuffer(pijob: Pijob): BigDecimal? {
        try {
            var p = getBuffer(pijob)
            return p.value
        } catch (e: Exception) {
            throw e
        }
    }

    fun getBuffer(pijob: Pijob): ReadBuffer {

        logger.debug("Buffer ${buffer}")
        for (i in buffer) {

            if (pijob.id.equals(i.pijob?.id)) {
                if (checktimeout(Date(), i.timeout!!))
                    return i
                throw Exception("Time out")
            }
        }
        throw Exception("Not found Buffer")

    }

    fun checktimeout(now: Date, timeout: Date): Boolean {
        if (now.time < timeout.time)
            return true

        return false
    }

    fun savebuffer(pijob: Pijob, value: BigDecimal) {
        var p: ReadBuffer? = null
        try {
            p = getBuffer(pijob)
        } catch (e: Exception) {
            logger.error(e.message)
        }

        var timeout = Date(Date().time + 1000 * 60 * 60)
        logger.debug("Timeout : ${timeout}")
        if (p != null) {
            logger.debug("Update old value  ${p}")
            p.readtime = Date()
            p.value = value
            p.timeout = timeout
        } else {

            p = ReadBuffer(value, Date(), pijob, timeout)
            logger.debug("New value ${p}")
            buffer.add(p)
        }
        logger.debug("End save buffer")

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

    fun readLocalDht()
    {

    }
    companion object {
        internal var logger = LoggerFactory.getLogger(ReadUtil::class.java)
    }
}

class ReadBuffer(var value: BigDecimal? = null, var readtime: Date? = null, var pijob: Pijob? = null,
                 var timeout: Date? = null) {
    override fun toString(): String {
        return "${pijob?.id} ${readtime} ${timeout} ${value} "
    }
}