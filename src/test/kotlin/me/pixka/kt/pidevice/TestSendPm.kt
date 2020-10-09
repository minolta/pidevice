package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.s.HttpService
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestSendPm {

    @Test
    fun testSendPm() {
        var pm = Pm()
        pm.pm10 = BigDecimal(10)
        pm.pm1 = BigDecimal(1.1)
        pm.pm25 = BigDecimal(10)
        var pd = PiDevice()
        pd.mac = "99:99:99:99"
        pm.pidevice = pd

        var http = HttpService()
        http.post("http://192.168.88.21:2222/pm/add", pm,500)


    }
}