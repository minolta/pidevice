package me.pixka.kt.pidevice.directtorun

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.PijobService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class Testdirecttorun {

    @Autowired
    lateinit var pijobService: PijobService

    fun addJob(n:String?="test")
    {
        var job = Pijob()
        job.name = "TEst"
        job.refid = 99

        pijobService.save(job)


    }
    @Test
    fun testRun()
    {
        addJob()

        var p = pijobService.findByRefid(99)
        Assertions.assertNotNull(p)
    }
}