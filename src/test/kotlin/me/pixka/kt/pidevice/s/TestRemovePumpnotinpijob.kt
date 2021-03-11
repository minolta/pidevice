package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestRemovePumpnotinpijob {

    @Autowired
    lateinit var pus:PumpforpijobService

    @Autowired
    lateinit var pjs:PijobService

    @Autowired
    lateinit var pds:PideviceService

    @Autowired
    lateinit var loadpumpService: LoadpumpService


    @Test
    fun testRemovenotUsePumpsfromPijob()
    {
        var pijob = Pijob()
        pijob.name = "test remove pumps"

        pijob = pjs.save(pijob)

        var d1 = pds.findOrCreate("1")
        var d2 = pds.findOrCreate("2")

        var pij = Pumpforpijob()
        pij.pidevice = d1
        pij.pijob = pijob
        pij.refid = 1
        pij = pus.save(pij)

        var pij1 = Pumpforpijob()
        pij1.pidevice = d2
        pij1.pijob = pijob
        pij1.refid = 2
        pij1 = pus.save(pij1)

        Assertions.assertTrue(pus.all().size>0)

        var newpumps = listOf(pij1)

        loadpumpService.resetPumps(newpumps,pijob)

        Assertions.assertTrue(pus.all().size==1)
    }
}