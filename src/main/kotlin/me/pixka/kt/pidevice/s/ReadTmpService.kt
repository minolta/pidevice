package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Service
class ReadTmpService(val httpService: HttpService,val lgs:LogService) {

    var om = ObjectMapper()
    fun readTmp(ip: String): Tmpobj {
        try {
            var re = httpService.get("http://${ip}",2000)
            var t = om.readValue<Tmpobj>(re)
            return t
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),"ReadTmpService",
                    "","21","readTmp()","",null)
            throw e
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Tmpobj(var t: BigDecimal? = null, var tmp: BigDecimal? = null,
             val ip: String? = null, val mac: String? = null)
{
    fun getTmp(): Double? {
        if(tmp!=null)
            return tmp!!.toDouble()
        if(t!=null)
            return t!!.toDouble()

        return null
    }
}
