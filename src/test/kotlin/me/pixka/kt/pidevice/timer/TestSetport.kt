package me.pixka.kt.pidevice.timer

import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestSetport
{

    @Autowired
    lateinit var mp:MactoipService
    @Test
    fun testSetport()
    {


    }
}