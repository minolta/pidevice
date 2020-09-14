package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL

@Service
class ReadTmpService {
    var om = ObjectMapper()
    fun checkTmp(p: Pijob) {
        var t = readTmp(p.desdevice?.ip!!)

    }

    fun readTmp(ip: String): Tmpobj {
        try {
            var url = URL("http://${ip}")
            var c = url.openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.connectTimeout = 2000
            val buf = BufferedReader(
                    InputStreamReader(
                            c.inputStream))
            val response = StringBuilder()
            var inputLine: String? = ""
            while (buf.readLine().also({ inputLine = it }) != null)
                response.append(inputLine)
            var re = response.toString()
            var t = om.readValue<Tmpobj>(re)
            return t
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Tmpobj(var t: BigDecimal? = null, var tmp: BigDecimal? = null,
             val ip: String? = null, val mac: String? = null)