package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.PideviceService
import org.springframework.stereotype.Service

@Service
class TestService(val pds:PideviceService) {

    fun portstatusinjob(): List<Portstatusinjob> {

        var p1 = Portstatusinjob()

        p1.device = pds.findOrCreate("test1")
        p1.runtime = 100

        var p2 = Portstatusinjob()
        p2.device = pds.findOrCreate("test2")
        p2.runtime = 100

        return listOf(p1,p2)

    }
}