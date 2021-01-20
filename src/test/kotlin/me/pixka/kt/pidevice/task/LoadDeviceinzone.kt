package me.pixka.kt.pidevice.task

import me.pixka.kt.pibase.d.DeviceinzoneService
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class LoadDeviceinzone {

    @Autowired
    lateinit var dizs: DeviceinzoneService

    @Autowired
    lateinit var mts: MactoipService

    @Test
    fun testload() {

    }
}