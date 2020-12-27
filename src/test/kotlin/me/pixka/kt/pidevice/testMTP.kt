package me.pixka.kt.pidevice

import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pidevice.s.ReadDhtService
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Test

class TestMtp()
{


    @Test
    fun TestdispDHT()
    {
        val http = spyk<HttpService>()
        val lgs = mockk<LogService>(relaxed = true)
        var dhts = ReadDhtService(http,lgs)

        var  dhtobj =  dhts.iptodhtobj("192.168.89.55")
        println(dhtobj.h)
    }
}