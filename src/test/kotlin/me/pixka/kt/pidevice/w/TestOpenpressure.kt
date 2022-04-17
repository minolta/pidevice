package me.pixka.kt.pidevice.w

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.WarterLowPressureService
import me.pixka.kt.run.D1hjobWorker
import org.junit.jupiter.api.Test


class TestOpenpressure {

    @Test
    fun testopenpressure() {
        var pijob = Pijob()
        pijob.id = 1
        var ports = ArrayList<Portstatusinjob>()
        var port = Portstatusinjob()
        ports.add(port)
        var mtp = mockk<MactoipService>()
        var d1hjobWorker = D1hjobWorker(
            pijob,
            mtp,
            mockk<NotifyService>(relaxed = true),
            mockk<WarterLowPressureService>(relaxed = true)
        )
        every { mtp.getPortstatus(pijob,true) } returns ports
        every { mtp.setport(port,5) } returns "{ok}"
        d1hjobWorker.openportdownpressure()

        verify { mtp.getPortstatus(pijob,true) }
    }
}