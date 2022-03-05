package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.s.PideviceService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestUpdatedeviceinfo {
    @Autowired
    lateinit var ps: PideviceService

    @Test
    fun testLoopupdate() {


        var local = ArrayList<PiDevice>()
        var fromnet = ArrayList<PiDevice>()

        var p1 = PiDevice()
        p1.ip = "1"
        p1.mac = "1"
      p1 =   ps.save(p1)
        local.add(p1)

        var p2 = PiDevice()
        p2.ip = "2"
        p2.mac = "2"
        ps.save(p2)


        var p3 = PiDevice()
        p3.ip = "1111"
        p3.mac = "1"

//        fromnet.add(p2)
        fromnet.add(p3)

        local.forEach {
            var found = findDevice(it, fromnet)
            if (found != null) {
                Assertions.assertTrue(found.ip.equals("1111"))
                ps.save(found)

                var indb = ps.findByMac("1")
                if(indb!=null)
                {
                    Assertions.assertTrue(indb.ip.equals("1111"))
                }
            }
        }
    }

    fun findDevice(device: PiDevice, fromnet: ArrayList<PiDevice>): PiDevice? {

        var found = fromnet.find { it.mac.equals(device.mac) }
        println(found)
        if (found != null) {
            if (!device.ip.equals(found.ip) || !device.name?.equals(found.name)!!) {
                device.ip = found.ip
                device.name = found.name
                return device
            }

        }
        return null
    }
}