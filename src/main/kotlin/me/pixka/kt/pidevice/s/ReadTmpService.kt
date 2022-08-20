package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.SensorinjobService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@Service
class ReadTmpService(val httpService: HttpService,val lgs:LogService,val sijs:SensorinjobService) {

    var om = ObjectMapper()


    fun readTmp(job:Pijob): ArrayList<Tmpobj> {
        var listofTmp = ArrayList<Tmpobj>()
        var sensorinjobs =    sijs.findByPijob_id(job.id)
        if(sensorinjobs?.size!! > 0) {
            sensorinjobs.forEach {

                println(it.sensor?.ip)
                try {
                    var re = readTmp(it.sensor?.ip!!)
                    re.device = it.sensor
                    listofTmp.add(re)
                } catch (e: Exception) {
                    println("************ ${e} **************")
                }


            }

        }
        else
        { //ถ้าไม่มีการเพิ่ม Sensor in pijob
            var re = readTmp(job.desdevice?.ip!!)
            re.device = job.desdevice
            listofTmp.add(re)
        }

        return listofTmp
    }
    fun readTmp(ip: String): Tmpobj {
        try {
            var re = httpService.get("http://${ip}",2000)
            var t = om.readValue<Tmpobj>(re)
            return t
        } catch (e: Exception) {
            logger.error("Read tmp Service Read TMP ${e.message} IP:${ip}")
            lgs.createERROR("${e.message}",Date(),"ReadTmpService",
            Thread.currentThread().name,"23","readTmp()",
            "",null,null)
//            lgs.createERROR("${e.message}", Date(),"ReadTmpService",
//                    "","21","readTmp()","",null)
            throw e
        }
    }

    internal var logger = LoggerFactory.getLogger(ReadTmpService::class.java)
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Tmpobj(var t: BigDecimal? = null, var tmp: BigDecimal? = null,
             val ip: String? = null, val mac: String? = null,var device:PiDevice?=null)
{
    fun getTmp(): Double? {
        if(tmp!=null)
            return tmp!!.toDouble()
        if(t!=null)
            return t!!.toDouble()
        return null
    }

    override fun toString(): String {
        return "Device ${device}  ip ${ip}  tmp ${getTmp()}"
    }
}
