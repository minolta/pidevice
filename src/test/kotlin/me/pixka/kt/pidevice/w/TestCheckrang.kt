package me.pixka.kt.pidevice.w

import me.pixka.kt.pibase.d.Pijob
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TestCheckrang {

    @Test
    fun testCheckrang()
    {
        var pijob = Pijob()
        pijob.tlow = BigDecimal(1)
        pijob.thigh = BigDecimal(10)
        var p = 5.0
        var l = pijob.tlow!!.toDouble()
        var h = pijob.thigh!!.toDouble()

        Assertions.assertTrue(p in l..h)

    }
}