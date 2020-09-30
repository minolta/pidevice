package me.pixka.kt.pidevice

import io.mockk.*
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.run.OffpumpWorker
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


//@SpringBootTest
class TestOffPump {

    var df = SimpleDateFormat("HH:mm")

    //    @RelaxedMockK
//    lateinit var lgs:LogService
    @Test
    fun TestOffPump() {

        val ips = mockk<IptableServicekt>()
//        val httpService = mockk<HttpService>()
        val httpService = spyk<HttpService>()
        val lgs = mockk<LogService>(relaxed = true)

        var p = PiDevice()
        p.mac = "99:99:99:99"


        every { ips.findByMac("99:99:99:99") } returns Iptableskt("Test", "192.168.89.247",
                "", Date(), Date())
//        every { httpService.get("http://192.168.88.29/off") } returns "{\n\"name\": \"P2 \"}"
//        every { httpService.get("http://192.168.89.247/off") } answers{
//            TimeUnit.SECONDS.sleep(10)
//            throw Exception("null")
//        }


        var job = Pijob()
        job.name = "Test off"
        job.desdevice = p
        job.runtime = 10

        var off = OffpumpWorker(job, httpService, ips, lgs)
        off.run()
        Assertions.assertTrue(off.isRun)



//        every { httpService.get("http://192.168.88.29/off") } returns ""
//        off.run()
//        Assertions.assertTrue(off.isRun)

//        verify { httpService.get("http://192.168.89.247/off") }
//        verify { ips.findByMac("99:99:99:99") }
//        confirmVerified(httpService)
//        confirmVerified(ips)
    }
}