package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pidevice.c.Statusobj
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestReadPressure {


    @Autowired
    lateinit var mtp: MactoipService


    @Test
    fun testReadPressure() {
        try {
            var re = mtp.http.getNoCache("http://192.168.89.66/")
            var s = mtp.om.readValue<Statusobj>(re)
            println(s.psi)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}