package me.pixka.kt.pidevice.t

import io.mockk.every
import io.mockk.mockk
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.PressureOverWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal


class TestPressureInRang {



    @Test
    fun TestPressureOver()
    {
        var mtp = mockk<MactoipService>(relaxed = true)
        var ntf = mockk<NotifyService>(relaxed = true)
        var pd = PiDevice()
        var pjob = Pijob()
        pjob.tlow = BigDecimal(5)
        pjob.thigh = BigDecimal(20)
        pjob.hlow = BigDecimal(1)
        pjob.desdevice = pd

        var po = PressureOverWorker(pjob,mtp,ntf)

        every { mtp.readPressure(pd) } returns 1.0
        every { mtp.readPressure(pd,2000) } returns 10.0

        po.run()
        println(po.isRun)
        println(po.exitdate)
        println(po.status)
        Assertions.assertTrue(po.isRun)

    }
}