package me.pixka.kt.pidevice

import io.mockk.mockk
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat

class TestTimeServiceII {

    val df = SimpleDateFormat("hh:mm")

    @Test
    fun TestChecktimeService() {

        var lgs = mockk<LogService>(relaxed = true)
        var cts = CheckTimeService(lgs)

        var pj = Pijob()

        pj.etimes = null
        pj.stimes = null
        Assertions.assertTrue(cts.checkTime(pj, df.parse("15:00")))



        pj.etimes = "19:00"
        pj.stimes = "8:00"

        Assertions.assertTrue(cts.checkTime(pj, df.parse("15:00")))

        pj.etimes = "19:00"
        Assertions.assertTrue(cts.checkTime(pj, df.parse("15:00")))

        pj.stimes ="9:00"
        Assertions.assertTrue(cts.checkTime(pj, df.parse("15:00")))



    }
}