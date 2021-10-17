package me.pixka.kt.pidevice.d

import io.mockk.every
import io.mockk.mockk
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Job
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.D1hjobWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestDirectorun {

    @Autowired
    lateinit var ps: PijobService

    @Autowired
    lateinit var js: JobService

    @Autowired
    lateinit var pds: PideviceService

    @Autowired
    lateinit var taskService:TaskService


    var pidevice: PiDevice? = null

    fun addJob(n: String = "test create pijob", refid: Long = 1000, job: Job? = null, d: PiDevice?): Pijob {
        var pijob = Pijob()
        pijob.name = n
        pijob.refid = refid
        pijob.job = job
        pijob.desdevice = d
        return ps.save(pijob)
    }

    @Test
    fun run() {

        var jobtype = js.findorcreate("hjob")
        var pd = PiDevice()
        pd.name = "name"
        pd.ip = "192.168.88.99"
        pd.mac = "11:11:11:11"
//        pd = pds.save(pd)
        var d = pds.findOrCreate(pd)
        var job = addJob("test job", 1000, jobtype,d)

        Assertions.assertTrue(job != null)

        var byrefid = ps.findByRefid(1000L)

        Assertions.assertNotNull(byrefid)
        runjob(job)
    }

    fun runjob(pijob: Pijob) {
        val mtp = mockk<MactoipService>()
        val ntf = mockk<NotifyService>()


        if (pijob.job!!.equals("hjob")) {
            println("Hjob ")
            var task = D1hjobWorker(pijob, mtp, ntf)
            var run = taskService.run(task)
            Assertions.assertTrue(run)
        }
    }
}