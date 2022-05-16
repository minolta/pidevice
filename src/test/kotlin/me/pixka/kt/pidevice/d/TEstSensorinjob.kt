package me.pixka.kt.pidevice.d

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Sensorinjob
import me.pixka.kt.pibase.d.SensorinjobService
import me.pixka.kt.pibase.o.HObject
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TEstSensorinjob {

    @Autowired
    lateinit var ps: PideviceService

    @Autowired
    lateinit var sijs: SensorinjobService


    @Autowired
    lateinit var pjs: PijobService

    @Autowired
    lateinit var mtp: MactoipService
    val om = ObjectMapper()
    @Test
    fun addSij() {
        var device = ps.findOrCreate("device1")
        var sensor1 = ps.findOrCreate("sensor1")
        var sensor2 = ps.findOrCreate("sensor2")

        sensor1.ip = "192.168.89.187";
        sensor1 = ps.save(sensor1)
        var pijob = Pijob()
        pijob.name = "fortest"
        pijob = pjs.save(pijob)


        var sij = Sensorinjob()
        sij.name = "injso 1"
        sij.sensor = sensor1
        sij.pijob = pijob


        sijs.save(sij)

        sij = Sensorinjob()
        sij.name = "sensor 2"
        sij.pijob = pijob
        sij.sensor = sensor2
        sijs.save(sij)


        var si = sijs.findByPijob_id(pijob.id)

        Assertions.assertTrue(si?.size == 2)
        si?.forEach {
            println(it)
            try {
                var re = mtp.http.get("http://${it.sensor!!.ip}", 12000)
                var h = om.readValue<HObject>(re)
                println("*** H Value ${h.h} *****")
            }catch (e:Exception)
            {
                e.printStackTrace()
            }
        }

    }
}