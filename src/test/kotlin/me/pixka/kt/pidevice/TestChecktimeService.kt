package me.pixka.kt.pidevice

import io.mockk.mockk
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Test
import java.util.*

class TestChecktimeService {

    @Test
    fun TestChecktimeService()
    {
        var lgs = mockk<LogService>(relaxed = true)
        var pj = Pijob()
        pj.runtime = 100
        pj.waittime = 100

        var ct = CheckTimeService(lgs)

        println(Date())
        println(ct.findExitdate(pj))

    }
}