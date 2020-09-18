package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.s.HttpService
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*


class TestSendpressure
{


    @Test
    fun testSendPressureValue()
    {
        var pd = PiDevice()
        pd.name = "test"
        pd.mac = "99:99:99:99"
        val om = ObjectMapper()
        var httpService = HttpService()
        var pv = PressureValue()
        pv.pressurevalue = BigDecimal("40.2")
        pv.valuedate = Date()
        pv.device = pd
        var re = httpService.post("http://pi3.pixka.me:2222/pressure/add",pv)

        println(re)
    }
}