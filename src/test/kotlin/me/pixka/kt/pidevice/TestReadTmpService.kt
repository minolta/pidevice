package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.pidevice.s.Tmpobj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

@SpringBootTest
@ActiveProfiles("test")
class TestReadTmpService {
    var om = ObjectMapper()
    @Autowired
    lateinit var service:ReadTmpService
    fun readTmp(ip: String): Tmpobj {
        try {
            println("call")
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
            c.disconnect()
            var t = om.readValue<Tmpobj>(re)
            return t
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun h(t:Throwable?): Tmpobj {

        return Tmpobj()
    }

    @Test
    fun testReadTmpService() {
        var t:Tmpobj? = null
        CompletableFuture.supplyAsync(Supplier {
            var tmp = service.readTmp("192.168.89.98")
            println(tmp)
            t = tmp
            tmp
        }).thenApply {
            println("===========>:" + it.t)
        }.exceptionally {  t: Throwable? ->  println("TEST ERROR ${t?.message}")  }
        TimeUnit.SECONDS.sleep(10)

        Assertions.assertTrue( t != null)
    }
}