package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.LoadpumpService
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class LoadPump {

    @Autowired
    lateinit var mtp: MactoipService

    @Autowired
    lateinit var pjs: PijobService
    @Autowired
    lateinit var pus:PumpforpijobService
    @Autowired
    lateinit var loadpumpService: LoadpumpService
    var alljobs: ArrayList<Pijob>? = null
    fun loadPijob() {
        var re: String? = mtp.http.get("http://192.168.88.21:3333/pijob/lists/00:e0:4c:68:07:ae", 500)
        alljobs = mtp.om.readValue<ArrayList<Pijob>>(re!!)
        println("********************")
        println(alljobs)
    }


    @Test
    fun loadpums() {

        // refid of job in pi device

        try {
            loadPijob()
            alljobs?.forEach {
                var re = mtp.http.get("http://192.168.88.21:3333/pump/${it.id}")
                var pumps = mtp.om.readValue<ArrayList<Pumpforpijob>>(re)
                var p = pumps.map {
                    it.refid = it.id
                }
                if(pumps.size>0) {

                    loadpumpService.savePumps(pumps,it)

                }
            }

            Assertions.assertTrue(pus.all().size==0)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}