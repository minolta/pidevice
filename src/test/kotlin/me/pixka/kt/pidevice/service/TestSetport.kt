package me.pixka.kt.pidevice.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.ReadDhtService
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Test
import java.util.*

class TestSetport {


    @Test
    fun TestFindUrl() {
        var ips = mockk<IptableServicekt>()
        every { ips.findByMac("99") } returns Iptableskt("", "192.168.89.15", "", Date(), Date())

        var device = mockk<PiDevice>()
        every { device.mac } returns "99"

        var pn = mockk<Portname>()
        every { pn.name } returns "D1"

        var lg = mockk<Logistate>()
        every { lg.toInt() } returns 1

        var pit = mockk<Portstatusinjob>()
        every { pit.device } returns device
        every { pit.runtime } returns 10
        every { pit.waittime } returns 10
        every { pit.portname } returns pn
        every { pit.status } returns lg

        var mtp = MactoipService(ips,mockk<LogService>(relaxed = true),
                spyk<HttpService>(),mockk<ReadDhtService>(relaxed = true),mockk<PortstatusinjobService>(),
            mockk<ReadTmpService>(relaxed = true))


        mtp.setport(pit)
    }
}