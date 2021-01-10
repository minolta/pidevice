package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.worker.DustcheckWorker
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestDustcheck {


    @Test
    fun TestDustCheck() {

        var d = PiDevice()
        d.mac = "00:00:00:00"
        var mtp = mockk<MactoipService>()
        var ntfs = mockk<NotifyService>(relaxed = true)
        var pijob = Pijob()
        pijob.id = 1
        pijob.tlow = BigDecimal(9)
        pijob.desdevice = d
        pijob.token = "xx"
        every { mtp.readStatus(pijob) } returns "{\"pm25\": 10}"
        every { mtp.om } returns ObjectMapper()
        var t = DustcheckWorker(pijob, mtp, ntfs)

        t.run()

        verify { ntfs.message("Pm2.5 9.0 > 10.0 ", "xx") }
    }
}