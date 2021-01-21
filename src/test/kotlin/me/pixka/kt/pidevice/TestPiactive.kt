package me.pixka.kt.pidevice

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.spyk
import me.pixka.kt.pidevice.c.Statusobj
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

/**
 * สำหรับทดสอบ check pi active
 */
@DataJpaTest
class TestPiactive {

    @Autowired
    lateinit var mtp:MactoipService
    @Test
    fun testConvert()
    {

        var re = "{\"uptime\": 100}"

        var status = mtp.om.readValue<Statusobj>(re)

        Assertions.assertNotNull(status.uptime)
    }

    @Test
    fun testCall()
    {
        var re = mtp.http.get("http://192.168.88.20:888")
        var status = mtp.om.readValue<Statusobj>(re)

        Assertions.assertNotNull(status.uptime)
    }
}