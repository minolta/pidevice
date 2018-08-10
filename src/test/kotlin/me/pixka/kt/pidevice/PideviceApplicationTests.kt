package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Logistate
import me.pixka.kt.pibase.d.Portstatusinjob
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
        val d = SimpleDateFormat("yyyy/MM/dd HH:mm")
        val dn = SimpleDateFormat("yyyy/MM/dd")
        var ds = dn.format(Date())
        println("DS: ${ds}")
        var datenow = dn.parse(ds)
        val c = Calendar.getInstance()
        c.time = datenow
        c.add(Calendar.DATE, 1)  // number of days to add
        var nextdate = dn.format(c.time)
        var nr = nextdate + " 09:00"
        var timetorun = d.parse(nr)

        println("*******************************  ${timetorun}  ********************************")

    }

}
