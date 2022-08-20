package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Sensorinjob
import me.pixka.kt.pibase.d.SensorinjobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestReadtmpSensor {

    @Autowired
    lateinit var pjs: PijobService

    @Autowired
    lateinit var sijs: SensorinjobService

    @Autowired
    lateinit var rts: ReadTmpService

    @Autowired
    lateinit var pds: PideviceService

    @Test
    fun testFindSensorinjobService() {
        var job = pjs.findOrCreate("Test find Sensor in job Service")
        var p = PiDevice()
        p.name = "p1"
        p.ip = "192.168.89.159"
        p = pds.save(p)

        var s1 = Sensorinjob()
        s1.pijob = job
        s1.sensor = p
        sijs.save(s1)

        p = PiDevice()
        p.name = "p2"
        p.ip = "192.168.89.47"
        p = pds.save(p)
        var s2 = Sensorinjob()
        s2.pijob = job
        s2.sensor=p
        sijs.save(s2)


        var sis = sijs.findByPijob_id(job.id)
        Assertions.assertTrue(sis?.size == 2)

        var list = rts.readTmp(job)
        println(list)
    }

    @Test
    fun testForloop()
    {
        var t = listOf<Int>(10,22,33)

        for(i in t)
        {
            println(i)
            if(i == 22)
                break
        }
    }
}