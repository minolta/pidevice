package me.pixka.kt.pidevice.s

import io.mockk.every
import io.mockk.mockk
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.d.ConfigdataService
import me.pixka.kt.run.D1hjobWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

@DataJpaTest
class TestLowPressureService {

    @Autowired
    lateinit var cs:ConfigdataService
    @Test
    fun testService() {
        var f = mockk<FindJob>()
        var ntfs = mockk<NotifyService>()
        var wls = WarterLowPressureService(f,ntfs)
        wls.lowpressureCount++

        Assertions.assertTrue(wls.lowpressureCount == 1)

        Assertions.assertTrue(wls.maxcount == 10)
        Assertions.assertTrue(wls.canuse)
        wls.reportLowPerssure(1L, 20.20)

        Assertions.assertTrue(wls.lowpressureCount > 1)
        Assertions.assertTrue(wls.reports.size == 1)
        wls.lowpressureCount = 5
        wls.reportLowPerssure(1L, 20.20)
        Assertions.assertTrue(wls.canuse)

        var lps = mockk<WarterLowPressureService>()
        var mtp = mockk<MactoipService>()
        var pijob = mockk<Pijob>()
        every { pijob.tlow } returns BigDecimal(0.0)
        every { lps.canuse } returns false
        every { lps.reset() } returns true
        var dh1worker = D1hjobWorker(pijob, mtp, ntfs, lps,cs)
        var pressureok = dh1worker.checkPressure(pijob)
        println(pressureok)
        var canrun = dh1worker.canrun()

        Assertions.assertTrue(!canrun)

        lps.reset()
        wls.reset()

        Assertions.assertTrue(wls.reports.size == 0)

    }
}