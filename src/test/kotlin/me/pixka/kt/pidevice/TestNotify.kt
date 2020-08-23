package me.pixka.kt.pidevice

import me.pixka.kt.pibase.s.ReadStatusService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TestNotify {

    @Autowired
    lateinit var readStatusService: ReadStatusService


    @Test
    fun readPSI() {
        var psi = readStatusService.readPSI("http://192.168.89.23")
        println(psi)
    }
}