package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.Tmpobj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class TestChecktmps {
    fun checktmps(tmps: List<Tmpobj>, job: Pijob): Boolean {
        for (i in tmps) {
            if (checktmp(i, job)) {
                return true
            }

        }
        return false
    }

    fun checktmp(t: Tmpobj, job: Pijob): Boolean {
        try {
            var tmp: Double = 0.0
            if (t.tmp != null) {
                tmp = t.tmp?.toDouble()!!
            } else if (t.t != null) {
                tmp = t.t?.toDouble()!!
            }
            if (job.tlow?.toDouble()!! <= tmp && job.thigh?.toDouble()!! >= tmp)
                return true
        } catch (e: Exception) {

        }
        return false

    }


    @Test
    fun testCheck()
    {
        var job = Pijob()
        job.tlow = BigDecimal(40.0)
        job.thigh = BigDecimal("100.0")

        var list = ArrayList<Tmpobj>()
        var t = Tmpobj()
        t.t = BigDecimal(-127)
        list.add(t)

        t = Tmpobj()
        t.t = BigDecimal(60.01)
        list.add(t)

       Assertions.assertTrue( checktmps(list,job))
    }

}