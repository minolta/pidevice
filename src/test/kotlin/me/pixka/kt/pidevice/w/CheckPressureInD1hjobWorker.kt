package me.pixka.kt.pidevice.w

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.d.ConfigdataService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.WarterLowPressureService
import me.pixka.kt.run.D1hjobWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

@DataJpaTest
class CheckPressureInD1hjobWorker {
    @Autowired
    lateinit var lps: WarterLowPressureService
    @Autowired
    lateinit var cs: ConfigdataService
    @Autowired
    lateinit var pjs: PijobService

    @Autowired
    lateinit var pus: PumpforpijobService

    @Autowired
    lateinit var pds: PideviceService

    fun checkPressure(p: Pijob): Boolean {

        var pij = pus.bypijobid(p.id)
        Assertions.assertNotNull(pij)

        var psi = m?.readPressure(pij?.get(0)?.pidevice!!)
        Assertions.assertTrue(psi!! > 0.0)

        if (psi == null) {
            return false
        }

        var setp = p.tlow?.toDouble()

        if (setp!! < psi)
            return true

        return false
    }

    var m: MactoipService? = null


    @Test
    fun runTestD1hjobCheckPerssure() {
        m = mockk<MactoipService>(relaxed = true)
        var pijob = Pijob()
        pijob.name = "test"
        pijob.pidevice = pds.findOrCreate("1")
        pijob.tlow = BigDecimal(30) //ค่าแรงดันต่ำสุดที่ระบบจะทำงาน
        pijob = pjs.save(pijob)


        var device1 = pds.findOrCreate("1")
        var pij = Pumpforpijob()
        pij.pidevice = device1
        pij.pijob = pijob

        pus.save(pij)

        var device2 = pds.findOrCreate("2")
        var pij1 = Pumpforpijob()
        pij1.pijob = pijob
        pij1.pidevice = device2
        pus.save(pij1)


        var data = listOf(pij, pij1)
        every { m?.readPressure(device1) } returns 50.00
        every { m?.pus?.bypijobid(pijob.id) } returns data
//        Assertions.assertTrue(checkPressure(pijob))
        var nfs = mockk<NotifyService>(relaxed = true)
        var worker = D1hjobWorker(pijob, m!!, nfs, lps,cs)
        Assertions.assertTrue(worker.checkPressure(pijob))
        verify { m?.readPressure(device1) }
    }

}