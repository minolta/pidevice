package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.run.Pmdata
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

//@SpringBootTest
class TestReadDust {

    val om = ObjectMapper()

    @Test
    fun TestReadDustWorker() {
        var re = HttpService().get("http://192.168.89.98/",5000)
        var pd = om.readValue<Pm>(re)
        println(pd)
    }


}