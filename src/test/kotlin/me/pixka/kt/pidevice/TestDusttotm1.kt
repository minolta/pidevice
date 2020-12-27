package me.pixka.kt.pidevice

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pidevice.o.Dustobj
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.concurrent.TimeUnit

@DataJpaTest
class TestDusttotm1 {

    @Autowired
    lateinit var mtc: MactoipService

    @Test
    fun test() {

        var duo = mtc.om.readValue<Dustobj>(mtc.http.getNoCache("http://192.168.89.10/"))
        mtc.http.getNoCache("http://192.168.88.14/settm1?value=${duo.pm25}")
//        var mtc = spyk<MactoipService>()
//
//        for (i in 0..10) {
//            mtc.http.getNoCache("http://192.168.88.14/settm1?value=${i}")
//            TimeUnit.SECONDS.sleep(5)
//        }
    }

}