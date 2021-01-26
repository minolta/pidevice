package me.pixka.kt.pidevice

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.o.PSIObject
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestReadPressure {

    @Autowired
    lateinit var mtp:MactoipService
    @Test
    fun testReadPressure()
    {
        var re = mtp.http.get("http://192.168.89.66/",10000)
        var o = mtp.om.readValue<PSIObject>(re)
        Assertions.assertNotNull(o.psi)
        println(o.psi)
    }


}