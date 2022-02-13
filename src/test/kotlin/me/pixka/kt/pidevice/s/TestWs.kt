package me.pixka.kt.pidevice.s

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PijobService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

@DataJpaTest
class TestWs {

    @Autowired
    lateinit var ws: WarterLowPressureService

    @Autowired
    lateinit var pjs: PijobService

    @Test
    fun find() {
        Assertions.assertNotNull(ws)
        Assertions.assertNotNull(ws.findJob)
        Assertions.assertNotNull(ws.ntfs)
    }

    @Test
    fun findMaxlowConfig() {
        var config = pjs.findOrCreate("MaxlowConfig")

        var configlow = ws.getNotifyConfig()

        Assertions.assertTrue(config.name.equals(configlow!!.name))
    }

    @Test
    fun testSetMaxDafault() {
        pjs.clear()
        var p = Pijob()
        p.name = "MaxlowConfig"
        p.thigh = BigDecimal(30)

        pjs.save(p)

        ws.setDefaultMaxCount()

        Assertions.assertTrue(ws.maxcount == 30)
    }

    @Test
    fun testSendmessage() {
        var ntfs = mockk<NotifyService>()
        var maxconfig = Pijob()
        maxconfig.token = "xxxx"
        maxconfig.thigh = BigDecimal(10)
        maxconfig.tlow = BigDecimal(10)
//        every { ntfs.message("Low pressure ", maxconfig.token!!) }
        var findJob = mockk<FindJob>(relaxed = true)

        var wss = mockk<WarterLowPressureService>()
        every { wss.ntfs.message("Low pressure ", maxconfig.token!!) } returns null
//        every {maxconfig.tlow} returns BigDecimal(10)
        every { ntfs.message("Low pressure ", maxconfig.token!!) } returns null
        every { ntfs.message("Low pressure ") } returns null
        every { wss.sendmessage(maxconfig) } returns Unit
        wss.sendmessage(maxconfig)

        verify { wss.sendmessage(maxconfig) }
    }

    @Test
    fun testSendmessage1() {

        var p = Pijob()
        p.token = "xxxx"

        var wss = mockk<WarterLowPressureService>()

        every { wss.ntfs.message("Low pressure",p.token!!) } returns null
        every { wss.sendmessage(p) } returns Unit
//        wss.ntfs.message("")
        wss.sendmessage(p)
//        verify(atLeast = 3) { wss.ntfs.message("Low pressure",p.token!!) }
    }
}