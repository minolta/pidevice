package me.pixka.kt.pibase.s

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.DS18sensor
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class SensorService(val dbconfigService: DbconfigService, val ps: PideviceService,
                    val dss: DS18sensorService, val iptableServicekt: IptableServicekt, val http: HttpControl) {
    private val om = ObjectMapper()
    /*  val ex = ThreadPoolExecutor(
              2,
              5,
              5, // <--- The keep alive for the async task
              TimeUnit.SECONDS, // <--- TIMEOUT IN SECONDS
              ArrayBlockingQueue(100),
              ThreadPoolExecutor.AbortPolicy() // <-- It will abort if timeout exceeds
      )
  */
    var readbuffer = ArrayList<DS18ReadBuffer>()

    fun findurl(desid: Long, sensorid: Long?): String? {
        var desdevice = ps.find(desid) //เปลียน ip แล้ว
        //var desdevice = ps.findByRefid(desid) //refid จะ save ตอน load pijob
        logger.debug("1 Find pidevice ${desid} found ===> ${desdevice} #readother")
        var sensor: DS18sensor? = null
        try {
            sensor = dss.find(sensorid!!)
        } catch (e: Exception) {
            logger.error("Find Sensor Error")
        }
        logger.debug("2 Find sensor ${sensorid} found ===> ${sensor} #readother")
        var ip = iptableServicekt.findByMac(desdevice?.mac!!)
        logger.debug("3 Find ip of pidevice ${desdevice} found ===> ${ip} #readother")
        if (ip == null || ip.ip == null) {
            logger.error("4 Can not find ip ${ip} #readother")
            return null
        }
        var url = ""




        if (ip != null && ip.ip != null) {
            if (sensor == null) {
                url = "http://${ip?.ip}/ktype"
            } else
                if (sensor?.name != null && sensor.name?.indexOf("28-") != -1)
                    url = "http://${ip?.ip}/ds18valuebysensor/${sensor.name}"
                else {
                    //เป็น ktype จะไม่มี 28- ให้อานตรงๆเลย
                    url = "http://${ip?.ip}/ktype"
                }
        }
        logger.debug("6 Read URL: [${url}] #readother")
        return url

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
            return null
        }
    }

    fun getValue(url: String): DS18value? {

        try {
            var value: String? = ""
            var get = HttpGetTask(url)
            var ee = Executors.newSingleThreadExecutor()
            var f = ee.submit(get)
            logger.debug("60 Start get value Thread => [${ee}] GET: ${get}  F${f}")
            try {
                logger.debug("61 ${f}")
                value = f.get(15, TimeUnit.SECONDS)
                logger.debug("62 Value from other ${value} #readother")
                var p = Stringtods18value(value!!)
                logger.debug("63 value [${p}]")
                return p
            } catch (e: Exception) {
                f.cancel(true)
                logger.error("64 Read other timeout  ${e.message} #readother")

            }
        } catch (e: Exception) {
            logger.error("65 ERROR ${e.message}")
        }
        return null
    }

    fun readDsOther(desid: Long, sensorid: Long?): DS18value? {

        try {

            var s = 0L
            var url = findurl(desid, sensorid)
            logger.debug("50 URL ${url}")
            if (sensorid != null) {
                s = sensorid
            }

            if (url != null) {
                var value = getValue(url)
                logger.debug("51 Value [${value}]")
                if (value != null) {
                    update(desid, s, value)
                    logger.debug("52 return new value ${value}")
                    return value
                }

                var oldvalue = readold(desid, s)
                if (oldvalue != null) {
                    if (checkage(oldvalue)) {
                        logger.debug("53 return old value ${value}")
                        return oldvalue.value
                    }
                }
                return null

            }


        } catch (e: Exception) {
            logger.error("17 Read DS OTher ${e.message} #readother")

        }
        return null
    }

    fun readold(desid: Long, sensorid: Long): DS18ReadBuffer? {
        var old = readBuffer(desid, sensorid)
        logger.debug("Try to read Old value in device : ${old}")
        if (old != null) {
            if (checkage(old)) {
                return old
            }
        }

        return null
    }

    fun checkage(old: DS18ReadBuffer): Boolean {
        logger.debug("Check age ")
        //21600000  = 6 ชม
        var readtimeout = dbconfigService.findorcreate("readcache", "21600000").value?.toLong()
        var now = Date().time
        var readtime = old.readdate.time
        var t = now - readtime

        logger.debug("Test Age ${t} $readtimeout")
        if (t > readtimeout!!) {
            logger.error("Data can not use")
            return false
        }

        logger.debug("Old data can use")
        return true


    }

    fun readBuffer(dis: Long, sid: Long): DS18ReadBuffer? {
        if (readbuffer.size < 1)
            return null

        for (b in readbuffer) {
            if (b.disid.equals(dis) && b.sensorid.equals(sid))
                return b
        }

        return null
    }

    /**
     * ใช้สำหรับ update ว่าอ่านเมื่อไหร่
     */
    fun update(did: Long, sid: Long, value: DS18value) {
        logger.debug("Update Read buffer new value  ")
        if (readbuffer.size > 0) {
            for (i in readbuffer) {
                if (i.disid.equals(did) && i.sensorid.equals(sid)) {
                    i.value = value
                    i.readdate = Date()
                    return
                }

            }
        }

        var n = DS18ReadBuffer(did, sid, value, Date())
        readbuffer.add(n)
        logger.debug("Buffer size ${readbuffer.size}")

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(SensorService::class.java)
    }
}

class DS18ReadBuffer(var disid: Long, var sensorid: Long, var value: DS18value, var readdate: Date)