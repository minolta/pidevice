package me.pixka.kt.pidevice

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.worker.DistanceWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestDistanceWorker
{

    @Test
    fun testDistanceWorker()
    {

        var http = spyk<HttpService>()

    }

    @Test
    fun testCheckDistance()
    {
        var mtp = mockk<MactoipService>(relaxed = true)
        var pijs = mockk<PortstatusinjobService>()
        var device = mockk<PiDevice>()
        var pijob = mockk<Pijob>()
        every { pijob.tlow } returns BigDecimal(1)
        every { pijob.thigh } returns BigDecimal(50)
        every { pijob.id } returns 1
        every { device.mac } returns ""
        var p  = mockk<Portstatusinjob>()
        every{ p.portname } returns Portname("D1")
        every { p.runtime  } returns 10
        every { p.waittime } returns 10
        every { p.device }

        var listofports  = listOf<Portstatusinjob>(p)

        every { pijs.findByPijobid(1) } returns listofports

        var worker = DistanceWorker(pijob,mtp,pijs)
        Assertions.assertTrue(worker.checkdistance(5.0))
    }
}