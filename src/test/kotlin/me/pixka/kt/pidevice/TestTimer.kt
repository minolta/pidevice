package me.pixka.kt.pidevice

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.Checkinrang
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.worker.D1TimerII
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

//@DataJpaTest
class TestTimer
{


    @Test
    fun testLow()
    {


        var mtp = mockk<MactoipService>()
        var line = mockk<NotifyService>(relaxed = true)
        var pijob = Pijob()
        pijob.tlow = BigDecimal(9)
        pijob.thigh = BigDecimal(10)
        pijob.runtime = 1

        var list = ArrayList<Portstatusinjob>()
        var p = Portstatusinjob()
        p.runtime = 10
        p.waittime  = 0
        list.add(p)

        every { mtp.setport(p) } returns "{ok}"
        every { mtp.readTmp(pijob) } returns BigDecimal(10)
        every { mtp.getPortstatus(pijob) } returns list
        var t = D1TimerII(pijob,mtp,line)


//        Assertions.assertTrue(t.waitlowtmp())
//        Assertions.assertTrue(t.waithightmp())
//        Assertions.assertTrue(t.waithightmp())

        t.run()
        println(Date())
        TimeUnit.SECONDS.sleep(15)
        Assertions.assertTrue(t.isRun)
        println(t.exitdate)





    }
}