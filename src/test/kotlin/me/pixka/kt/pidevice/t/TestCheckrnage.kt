package me.pixka.kt.pidevice.t

import io.mockk.mockk
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.worker.CheckdistancecheckWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestCheckrnage {

    @Test
    fun testCheckrnag() {
        var job = Pijob()
        job.tlow = BigDecimal(2)
        job.thigh = BigDecimal(10)
        var ntfs = mockk<NotifyService>(relaxed = true)
        var mac = mockk<MactoipService>(relaxed = true)
        var worker = CheckdistancecheckWorker(job, ntfs, mac)

        var re = worker.checkRang(5L)
        Assertions.assertTrue(re)
        re = worker.checkRang(20L)
        Assertions.assertTrue(!re)

    }
}