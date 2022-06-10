package me.pixka.kt.pidevice.f

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TestReadallpressure {

    @Autowired

    lateinit var mtp: MactoipService

    @Test
    fun readAllPressure() {
        try {
            var device = PiDevice()
            device.ip = "192.168.89.45"
            var psi = mtp.readPressure(device)
            println(psi)
            device.ip = "192.168.89.188"
            psi = mtp.readPressure(device)
            println(psi)
            device.ip = "192.168.89.186"
            psi = mtp.readPressure(device)
            println(psi)
            device.ip = "192.168.89.246"
            psi = mtp.readPressure(device)
            println(psi)
        } catch (e: Exception) {

        }
    }
}