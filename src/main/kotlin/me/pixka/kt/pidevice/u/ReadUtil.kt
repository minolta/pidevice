package me.pixka.kt.pidevice.u

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.DS18sensorService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
//@Profile("pi")
class ReadUtil(val ips: IptableServicekt, val http: HttpControl, val iptableServicekt: IptableServicekt,
               val dss: DS18sensorService, val ps: PideviceService,
               val pideviceService: PideviceService, val ss: SensorService) {
    val om = ObjectMapper()

    var buffer = ArrayList<ReadBuffer>()

    fun readPressureByjob(j: Pijob): PressureValue? {
        var des = j.desdevice
        if (des == null) {
            logger.error("${j.name} Device not found ${des}")
            return null
        }
        var url = "/pressure"

        var ip = ips.findByMac(des.mac!!)
        if (ip != null) {
            var ipstring = ip.ip
            var re: String? = null
            var ee = Executors.newSingleThreadExecutor()
            try {
                var u = "http://${ipstring}${url}"
                logger.debug("${j.name}  Read pressure ${u}")
                var get = HttpGetTask(u)

                var f = ee.submit(get)
                re = f.get(2, TimeUnit.SECONDS)
            } catch (e: Exception) {
                logger.error(e.message)
                ee.shutdownNow()
                throw e
            }
            try {
                logger.debug("${j.name}  Pase value")
                var ps = om.readValue<PressureValue>(re, PressureValue::class.java)
                logger.debug("${j.name}  Pressure value ${ps}")
                return ps
            } catch (e: Exception) {
                logger.error(e.message)
                throw  e
            }
        }
        logger.error("${j.name}  Can not find ip")
        throw Exception("Can not find ip")


    }

    fun readKtype(p: Pijob): DS18value? {
        if (p.desdevice == null)
            throw Exception("Destinition error")

        var ip: Iptableskt? = null

        try {
            ip = ips.findByMac(p.desdevice?.mac!!)
            if (ip != null) {
                var ipstring = ip.ip
                var t = Executors.newSingleThreadExecutor()
                var u = "http://${ipstring}/ktype"
                var get = HttpGetTask(u)
                var f = t.submit(get)
                try {
                    var re = f.get(30, TimeUnit.SECONDS)
                    var o = om.readValue(re, DS18value::class.java)
                    logger.debug("${p.name} Get tmp ${o}")
                    return o
//                    status = "${p.name} Get pressure ${o}"
                } catch (e: Exception) {
                    logger.error("Read ktype error ${e.message}")
                    throw e
                }
            }
        } catch (e: Exception) {
            logger.error("Read Ktype ${e.message}")
            throw e
        }
        return null
    }

    fun readA0(desid: Long): Int? {
        var ee = Executors.newSingleThreadExecutor()
        try {
            var desdevice = findDevice(desid)
            var ip = findIp(desdevice)

            var url = "http://${ip?.ip}/a0"
            var get = HttpGetTask(url)

            var f = ee.submit(get)
            var value = f.get(2, TimeUnit.SECONDS)

            var a0value = value?.toInt()
            return a0value
        } catch (e: Exception) {
            logger.error("Read A0 ERROR ${e.message}")
            ee.shutdownNow()
            throw e
        }
    }

    fun readOther(desid: Long, senid: Long? = null): DS18value? {


        var url: String? = null

        try {
            url = findurl(desid, senid)
        } catch (e: Exception) {
            logger.error("readother error ${e.message}")
            throw e
        }
        logger.debug("readother url for ${url}")
        var ee = Executors.newSingleThreadExecutor()
        try {
            var get = HttpGetTask(url!!)
            var f = ee.submit(get)
            var value = f.get(2, TimeUnit.SECONDS)
            if (value != null) {
                return Stringtods18value(value)
            }
            throw Exception("Not found Ds18value ")
        } catch (e: Exception) {
            logger.error("read other error ${e.message}")
            ee.shutdownNow()
            throw e
        }


    }

    fun findDevice(desid: Long): PiDevice {
        try {
            var desdevice = ps.find(desid) //?????????????????? ip ????????????
            //var desdevice = ps.findByRefid(desid) //refid ?????? save ????????? load pijob
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
        try {
            var m = desdevice.mac?.toUpperCase()
            var ip = iptableServicekt.findByMac(m!!)
            logger.debug("3 Find ip of pidevice [${desdevice}] found ===> [${ip}] #readother findip")
            return ip
        } catch (e: Exception) {
            logger.error("Find ip ERROR ${e.message} findip")
            throw e
        }
    }

    fun findurl(desid: Long, sensorid: Long?): String? {
        var desdevice: PiDevice? = null
        try {
            desdevice = findDevice(desid)
        } catch (e: Exception) {
            logger.error("${desdevice?.name} findurl URL ${e.message}")
            throw Exception("${desdevice?.name} findurl URL ${e.message}")
        }
        logger.debug("${desid}  findurl Device name ${desdevice} ${desdevice.mac}")
        var sensor: DS18sensor? = null
        try {
            sensor = dss.find(sensorid!!)
        } catch (e: Exception) {
            logger.error("${sensorid}  findurl Sensor Error ${e.message}")
            throw  Exception("${sensorid}  findurl Sensor Error ${e.message}")
        }
        logger.debug("${desdevice.name}  2 Find sensor [${sensorid}] found ===> [${sensor}] #readother findurl")

        var ip: Iptableskt? = null
        try {
            ip = findIp(desdevice)
        } catch (e: Exception) {
            logger.error("${desdevice.name}  findurl findip ${e.message} findurl")
            throw Exception("${desdevice.name}  findurl findip ${e.message} findurl")
        }
        var url = ""

        if (ip != null && ip.ip != null) {
            if (sensor == null) {
                logger.error("${desdevice.name}  Sensor is null ${sensor}")
                url = "http://${ip.ip}/ktype"
            } else
                if (sensor.name != null && sensor.name?.indexOf("28-") != -1)
                    url = "http://${ip.ip}/ds18valuebysensor/${sensor.name}"
                else {
                    //???????????? ktype ????????????????????? 28- ???????????????????????????????????????
                    url = "http://${ip.ip}/ktype"
                }
        }
        logger.debug("${desdevice.name}  findurl 6 Read URL: [${url}] #readother")
        return url

    }

    /**
     * ???????????????????????????????????????????????? pijob ????????? ?????????????????? ?????????????????????????????????????????????????????????
     */
//    fun checkLocalPressure(pijob: Pijob): Boolean {
//        try {
//            logger.debug("Run localpressure")
//            var now = infoService.A0?.psi?.toDouble()
//            if (now == null)
//                throw Exception("Can not read localpressure")
//
//            logger.debug("Read local localpressure ${now}")
//            if (pijob.hlow != null && pijob.hhigh != null) {
//
//                var l = pijob.hlow?.toDouble()
//                var h = pijob.hhigh?.toDouble()
//
//                logger.debug("check localpressure l:${l} <= ${now}  <= ${h}")
//                if (l!! <= now && now <= h!!) {
//                    logger.debug("In rang localpressure")
//                    return true
//                }
//                logger.debug(" pressure in  rang localpressure ")
//                return false
//            }
//
//        } catch (e: Exception) {
//            logger.error("${e.message} localpressure")
//            throw e
//        }
//        throw Exception("Pressure not set localpressure")
//
//    }
//
//    fun readLocal(job: Pijob): DS18value? {
//        var localsensor = dss.find(job.ds18sensor_id!!)
//        logger.debug("4 Found local sensor ? ${localsensor} #readtmpbyjob")
//        var value: DS18value? = null
//        if (localsensor != null && value == null) {
//            logger.debug("5 Read Temp from ${localsensor} ")
//            var v = null
//            try {
//                var v = io.readDs18()
//            } catch (e: Exception) {
//                logger.error("6 ${e.message}")
//                throw e
//            }
//            value = DS18value()
//            value.t = v
//            logger.debug("7 Read Temp from ${localsensor}  get ${v} return ${value} #readtmpbyjob")
//            if (v != null)
//                return value
//        }
//        if (value != null) {
//            return value
//        }
//        throw Exception("Read local not found")
//
//    }

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

    fun checkpressure(p: Pijob): Boolean {

        try {
            var tmp = readPressureByjob(p)
            if (tmp != null) {
                var tl = p.hlow?.toFloat()
                var th = p.hhigh?.toFloat()
                var v = tmp.pressurevalue?.toFloat()

                if (v != null && tl != null) {
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
     * ????????????????????????????????????????????????????????? ???????????????????????????????????????????????????????????? local  ????????????????????????????????????????????? ????????? ??????????????????????????????
     */
    fun readTmpByjob(job: Pijob): BigDecimal? {
        try {
            var desid = job.desdevice_id
            var sensorid = job.ds18sensor_id

            logger.debug("readtmpbyjob desid : ${desid} sensorid : ${sensorid}")

            var value: DS18value? = null
            if (desid != null) {
                try {
                    value = readOther(desid, sensorid)
                    if (value != null) {
                        savebuffer(job, value.t!!)
                        return value.t
                    }
                } catch (e: Exception) {
                    logger.error("readtmpbyjob error ${e.message} ")
                    throw Exception("readtmpbyjob error ${e.message} ")
                }

                try {
                    var p = readBuffer(job)
                    return p
                } catch (e: Exception) {
                    logger.error("readtmpbyjob Save buffer ${e.message}")
                    throw Exception("readtmpbyjob Save buffer ${e.message}")
                }
            }


//            try {
//                value = readLocal(job)
//                if (value != null) {
//                    savebuffer(job, value.t!!)
//                    return value.t
//                }
//            } catch (e: Exception) {
//                logger.error("readtmpbyjob readlocal error ${e.message}")
//                try {
//                    var p = readBuffer(job)
//                    return p
//                } catch (e: Exception) {
//                    logger.error("readtmpbyjob Read buffer error ${e.message}")
//                    throw Exception("readtmpbyjob Read buffer error ${e.message}")
//                }
//            }


        } catch (e: Exception) {
            logger.error("readtmpbyjob 8 ${e.message}")
            throw Exception("readtmpbyjob 8 ${e.message}")
        }

        throw Exception("Not found readTmpByjob")


    }

    fun readBuffer(pijob: Pijob): BigDecimal? {
        try {
            var p = getBuffer(pijob)
            return p.value
        } catch (e: Exception) {
            logger.error("New read buffer")
            throw e

        }
    }

    fun getBuffer(pijob: Pijob): ReadBuffer {

        logger.debug("Buffer ${buffer}")
        for (i in buffer) {
            if (i.equals(pijob)) {
                return i
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
        logger.debug("savebuffer Timeout : ${timeout}")
        if (p != null) {
            var t = checktimeout(Date(), p.timeout!!)
            if (t) {
                var index = buffer.indexOf(p)
                logger.debug("savebuffer ${pijob.name} Update old value  ${p}")

                buffer[index].readtime = Date()
                buffer[index].value = value
                buffer[index].timeout = timeout
                logger.debug("savebuffer ${pijob.name} ")
            } else {
//                var removeok = buffer.remove(p) //????????????????????????????????????????????????
                var index = buffer.indexOf(p)
                buffer[index] = ReadBuffer(value, Date(), pijob, timeout)
                logger.debug("savebuffer ${pijob.name} New value ${p} remove is buffer size ${buffer.size} ")
            }
        } else {

            p = ReadBuffer(value, Date(), pijob, timeout)
            logger.debug("savebuffer ${pijob.name} New value ${p}")
            buffer.add(p)
        }
        logger.debug("savebuffer ${pijob.name} End save buffer")

    }

    /**
     * ??????????????????????????? ????????????????????? ktype ????????? D1
     */
    fun readTfromD1Byjob(job: Pijob): DS18value? {
        var ee = Executors.newSingleThreadExecutor()
        //mac to ip
        try {
            logger.debug("readtfromd1byjob Info Des device ${job.desdevice?.mac} ${job}")
            var ip = findip(job.desdevice!!.mac!!)

            logger.debug("readtfromd1byjob IP :${ip} ${job}")
            logger.debug("readtfromd1byjob MAC :${job.desdevice!!.mac} ${job}")
            var url = "http://${ip}/ktype"

            logger.debug("readtfromd1byjob read URL ${url} ${job}")
            var get = HttpGetTask(url)
            var f = ee.submit(get)
            var value = f.get(15, TimeUnit.SECONDS)
            if (value != null)
                return Stringtods18value(value)
            else
                throw Exception("Value is null")
        } catch (e: Exception) {
            logger.error("readtfromd1byjob ${e.message} ${job}")
            ee.shutdownNow()
            throw Exception("readtfromd1byjob ${e.message} ${job}")
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
            logger.debug("Found IP ${ip} findip")
            return ip!!.ip

        } catch (e: Exception) {
            logger.error("findip ${e.message}")
            throw Exception("findip ${e.message}")
        }
    }

    fun readLocalDht() {

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

    override fun equals(other: Any?): Boolean {

        if (other is ReadBuffer) {

            if (other.pijob?.id?.toInt() == pijob?.id?.toInt())
                return true
        }

        if (other is Pijob) {
            if (other.id.toInt() == pijob?.id?.toInt())
                return true

        }
        return false
    }
}