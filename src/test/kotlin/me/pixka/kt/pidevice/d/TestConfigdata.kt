package me.pixka.kt.pidevice.d

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestConfigdata {

    @Autowired
    lateinit var cs: ConfigdataService

    @Test
    fun testAdd() {

        var f = cs.findOrCreate("Test config","10000")

        println(f)
        Assertions.assertTrue(f.valuename?.toInt()== 10000)
    }
}