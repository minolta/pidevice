package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pibase.s.PortstatusinjobService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class Testresetpijob {
    @Autowired
    lateinit var pijobService: PijobService
    @Autowired
    lateinit var portstatusinjobService: PortstatusinjobService



    fun insertpijob() {

        var pijob = Pijob()

        for(i in 0..100) {
            pijob.name = "test${i}"
            pijob.priority = 10

            pijob = pijobService.save(pijob)!!

            var psij = Portstatusinjob()
            psij.pijob = pijob

            portstatusinjobService.save(psij)
        }
    }

    @Test
    fun test() {

//        insertpijob()

        delete()
        println(pijobService.all())
    }


    fun delete()
    {
        pijobService.clear()
    }
}