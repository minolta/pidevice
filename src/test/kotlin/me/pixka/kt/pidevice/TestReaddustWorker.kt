package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.Pmdata
import me.pixka.kt.run.ReaddustWorker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//@SpringBootTest
@DataJpaTest
@ActiveProfiles("test", "debug")
class TestReaddustWorker {
    @Autowired
    lateinit var pideviceService: PideviceService
    val om = ObjectMapper()

    @Autowired
    lateinit var ts: TaskService

    @Autowired
    lateinit var service: PmService

    @Autowired
    lateinit var pijobService: PijobService

    @Autowired
    lateinit var findJob: FindJob

    @Autowired
    lateinit var jobService: JobService

    @Test
    fun testReaddustWorker() {
        var http = HttpGetTask("http://192.168.89.243")
        var t = Executors.newSingleThreadExecutor()
        var f = t.submit(http)
        var re = f.get()
        var pd = om.readValue<Pmdata>(re!!)
        var pid = pideviceService.findByMac(pd.mac!!)
        var pm = Pm()
        pm.pidevice = pid
        pm.pm1 = pd.pm1
        pm.pm10 = pd.pm10
        pm.pm25 = pd.pm25
        pm.valuedate = Date()
        pm = service.save(pm)
        Assertions.assertEquals(1, pm.id)
    }

    fun addjob() {
        var device = PiDevice()
        device.name = "p"
        device.ip = "192.168.89.243"
        device.id = 1
        device = pideviceService.save(device)
        var job = Job()
        job.name = "readdust"
        job = jobService.save(job)
        var pijob = Pijob()

        pijob.name = "test read dust"
        pijob.desdevice = device
        pijob.job = job
        pijob.runtime = 1
        pijobService.save(pijob)
    }

    @Test
    fun testThread() {

        addjob()

        var jobs = findJob.loadjob("readdust")

        if (jobs != null) {
            jobs.forEach {

                var t = ReaddustWorker(it, "192.168.89.243", service, om, pideviceService)
                Assertions.assertEquals(true, ts.run(t))

            }

        }

        var pms = service.all()
//        Assertions.assertEquals(1, pms?.size)
//        Assertions.assertEquals(1, jobs?.size)
    }

    @Test
    fun testDirecto() {
        addjob()

        var jobs = findJob.loadjob("readdust")
        if (jobs != null) {
            jobs.forEach {

                var t = ReaddustWorker(it, "192.168.89.243", service, om, pideviceService)
                var ee = Executors.newSingleThreadExecutor()
                var f = ee.submit(t)
                var rr = f.get()
                if(it.runtime!=null)
                    TimeUnit.SECONDS.sleep(it.runtime!!)
                if(it.waittime!=null)
                    TimeUnit.SECONDS.sleep(it.waittime!!)

                println("RRRRRRRRRRRRRR${rr}")
            }

        }

        var pms = service.all()
        Assertions.assertEquals(1, pms.size)
    }
}