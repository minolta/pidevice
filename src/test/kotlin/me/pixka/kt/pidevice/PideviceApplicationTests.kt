package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Logistate
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.u.ReadBuffer
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.text.SimpleDateFormat
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class PideviceApplicationTests {

    @Test
    fun contextLoads() {


        var df = SimpleDateFormat("hh:mm:ss")

        println(df.parse("10:20:22"))

    }

}
