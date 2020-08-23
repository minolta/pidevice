package me.pixka.kt.pidevice

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.ReadStatusService
import me.pixka.kt.pidevice.worker.NotifyPressureWorker
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class TestPressureJob {
    @Autowired
    lateinit var readStatusService: ReadStatusService

    @Autowired
    lateinit var notifyService: NotifyService

    @Test
    fun testPressureJob() {
        var job = Pijob()
        job.name = "TEST"
        job.tlow = BigDecimal(0.0)
        job.thigh = BigDecimal(5.0)
        job.token = "oxTQIdmioYpPrwiXEMbA9dr7w2DkiMFcdm0ZqQJI0wR"
        var ee = Executors.newSingleThreadExecutor()
        var n = NotifyPressureWorker(job, readStatusService, "http://192.168.89.233", notifyService)
        n.run()
        println(n.psi)
        println(n.run)
//        var f = ee.submit(n)
//
//        f.get(2,TimeUnit.SECONDS)


    }

}