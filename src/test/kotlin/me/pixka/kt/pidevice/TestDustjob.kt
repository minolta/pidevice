package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Job
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.worker.Dustworker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal
import java.util.concurrent.Executors

@DataJpaTest
class TestDustjob {

    @Autowired
    lateinit var findJob: FindJob

    @Autowired
    lateinit var pijobService: PijobService
    @Autowired
    lateinit var jobService: JobService
    fun addJob() {

       var p =  jobService.save(Job(0,"rundust"))
        var pijob = Pijob()
        pijob.job = p
        pijob.tlow = BigDecimal(10)
        pijob.thigh = BigDecimal(50)
        pijobService.save(pijob)

    }

    @Test
    fun testDustjob() {
        addJob()
        var f = findJob.loadjob("rundust")

        Assertions.assertEquals(1,f?.size)
    }



}