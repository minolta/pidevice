package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestCheckHSeneorWorker {


    @Test
    fun TestWorker() {

        var pijob = Pijob()
        pijob.tlow = BigDecimal(10)
        pijob.thigh = BigDecimal(100)
        pijob.hlow = BigDecimal(10)
        pijob.hhigh = BigDecimal(100)
        pijob.token = "xxx"

        var ports = ArrayList<Portstatusinjob>()

        var pidevice = PiDevice()
        pidevice.mac = "00:00"
        var psij = Portstatusinjob()
        psij.device = pidevice

        ports.add(psij)
        pijob.ports = ports
        var mtp = mockk<MactoipService>(relaxed = true)
        var line = mockk<NotifyService>(relaxed = true)


        every { mtp.mactoip("00:00") } returns "192.168.0.0"
        every { mtp.getPortstatus(pijob) } returns ports
        every { mtp.http.get("http://192.168.0.0", 60000) } returns "{\"t\":10.0,\"h\":10.0}"


        var w = CheckHsensorWorker(pijob, mtp, line)

        w.run()

        verify { mtp.mactoip(pidevice.mac!!) }
        verify { mtp.http.get("http://192.168.0.0", 60000) }

        Assertions.assertTrue(w.isRun)
    }
}