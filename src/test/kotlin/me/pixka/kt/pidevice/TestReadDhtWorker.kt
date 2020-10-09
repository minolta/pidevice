package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pibase.d.DHTObject
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DhtvalueService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.run.ReadDhtWorker
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestReadDhtWorker {


    @Test
    fun TestReadDhtWorker() {

        var pds = mockk<PideviceService>()
        var httpservice = spyk<HttpService>()
        var lgs = mockk<LogService>(relaxed = true)
        var dhts = mockk<DhtvalueService>()

        var dht = Dhtvalue()
        dht.t = BigDecimal(10)
        dht.h = BigDecimal(99)
        dht.ip  = null

        var s = om.writeValueAsString(dht)
        every { httpservice.get("http://192.168.89.26",500) } answers {
            s
        }

        every { dhts.save(dht) } returns dht

        var job = Pijob()
        job.name = "Test Read DHT "
        job.runtime=10


        var readDhtWorker = ReadDhtWorker(job, pds, httpservice, "192.168.89.26",lgs,dhts)
        readDhtWorker.run()
        Assertions.assertTrue(readDhtWorker.isRun)
    }

    val om = ObjectMapper()

    @Test
    fun TestgetDhtvalue()
    {
        var httpservice = spyk<HttpService>()
        var re = httpservice.get("http://192.168.89.26",500)
        var dht = om.readValue<DHTObject>(re)

        Assertions.assertTrue(dht.h?.toDouble()!!>50)
        Assertions.assertTrue(dht.t?.toDouble()!!>10)


    }

    @Test
    fun testSaveDhtValue()
    {
        val dhts = mockk<DhtvalueService>()

        var dhtvalue = Dhtvalue()
        dhtvalue.h = BigDecimal(99.29)
        dhtvalue.t = BigDecimal(20.2)

        every { dhts.save(dhtvalue) } returns dhtvalue
        var dht = dhts.save(dhtvalue)
        Assertions.assertTrue(dht.t?.toDouble()==20.2)
    }
}