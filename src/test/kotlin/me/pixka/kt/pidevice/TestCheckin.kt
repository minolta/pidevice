package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Devicecheckin
import me.pixka.kt.pibase.s.HttpService
import me.pixka.pibase.o.Infoobj
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class TestCheckin {
    var om = ObjectMapper()
    val httpService =HttpService()
    fun checkin(url: String): Devicecheckin {
        try {

            val i = Infoobj()
            i.ip = System.getProperty("fixip")
            i.mac = System.getProperty("mac")
            i.password = UUID.randomUUID().toString() // สร้าง ยฟหหไนพก
            var re = httpService.post(url,i,2000)
            println(re)
            var t = om.readValue<Devicecheckin>(re)
            return t
        } catch (e: Exception) {
            throw e
        }
    }

    @Test
    fun testCheckin() {
        checkin("http://localhost:8080/checkin")
    }
}