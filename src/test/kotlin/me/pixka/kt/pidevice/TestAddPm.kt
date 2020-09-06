package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.t.HttpPostTask
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TestAddPm
{

    @Test
    fun testAdd()
    {
        var pd = PiDevice()
        pd.name = "Test"
        pd.mac = "00:00:00:01"
        var pm = Pm()
        pm.pm10 = BigDecimal(10)
        pm.pm1 = BigDecimal(20)
        pm.pm25 = BigDecimal(12)
        pm.pidevice = pd
        var h = HttpPostTask("http://localhost:8080/pm/add", pm)
        var f = Executors.newSingleThreadExecutor().submit(h)
        var r = f.get(4, TimeUnit.SECONDS)
        println(r)
    }
}