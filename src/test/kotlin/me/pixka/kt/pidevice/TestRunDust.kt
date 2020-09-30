package me.pixka.kt.pidevice

import io.mockk.every
import io.mockk.mockk
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.run.DustWorker
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*


class TestRunDust {

    @Test
    fun testRunDust() {
        val lgs = mockk<LogService>(relaxed = true)
        val ips = mockk<IptableServicekt>()
        val httpService = mockk<HttpService>()


        val mockKAdditionalAnswerScope = every { ips.findByMac("99:99:99:99") } returns Iptableskt("test ip",
                "192.168.88.1", "", Date(), Date())

        every { httpService.get("http://192.168.88.1/run?port=D1&delay=0&value=1&wait=0",500) }returns "{\"name\":\"test\"}"

        var p = PiDevice()
        p.mac = "99:99:99:99"
        p.name = "test device"
        var job = Pijob()
        job.runtime = 10
        job.desdevice = p
        var l = Logistate()
        l.name = "high"
        var   psij = Portstatusinjob(Portname("D1"), 1, 0, l,null,null,null)
        psij.device = p
        var worker = DustWorker(job, arrayListOf(

        psij), ips, httpService,lgs)

        worker.run()

        Assertions.assertTrue(worker.isRun)


    }

    @Test
    fun testCheckRunDustjob()
    {
        var job = mockk<Pijob>()
        var pm = mockk<Pm>()

        every { pm.pm25 } returns BigDecimal(15)
        every { job.tlow } returns BigDecimal(3)
        every { job.thigh } returns BigDecimal(100)
        var l = job.tlow
        var h = job.thigh
        var pm25 = pm.pm25

        Assertions.assertTrue (l!!.toDouble() <= pm25!!.toDouble() && pm25.toDouble() <= h!!.toDouble())

    }
}