package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Devicecheckin
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
    fun checkin(ip: String): Devicecheckin {
        try {
            var url = URL(ip)

            var encoding = Base64.getEncoder().encodeToString("USER_CLIENT_APP:password".toByteArray())
            //.encodeToString(("USER_CLIENT_APP:password").getBytes("UTF-8"));
            println(encoding)
            var c = url.openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.connectTimeout = 2000
            c.setRequestProperty("Content-Type", "application/json; utf-8")
            c.setRequestProperty("Accept", "application/json")
            c.setRequestProperty("Authorization", "Basic " + encoding)
            c.doOutput = true

            val i = Infoobj()
            i.ip = System.getProperty("fixip")
            i.mac = System.getProperty("mac")
            i.password = UUID.randomUUID().toString() // สร้าง ยฟหหไนพก
//            i.uptime = 10000;
            // สำหรับ ให้
            var jsonInputString = om.writeValueAsString(i)
            println(jsonInputString)
            c.outputStream.write(jsonInputString.toByteArray())
            c.outputStream.close()
//            c.outputStream.use({ os ->
//                val input: ByteArray = jsonInputString.toByteArray(Charset.forName("UTF-8"))
//                os.write(input, 0, input.size)
//            })
            val buf = BufferedReader(
                    InputStreamReader(
                            c.inputStream))
            val response = StringBuilder()
            var inputLine: String? = ""
            while (buf.readLine().also({ inputLine = it }) != null)
                response.append(inputLine)
            var re = response.toString()
            var t = om.readValue<Devicecheckin>(re)
            return t
        } catch (e: Exception) {
            throw e
        }
    }

    @Test
    fun testCheckin() {
        checkin("http://192.168.88.21:2222/checkin")
    }
}