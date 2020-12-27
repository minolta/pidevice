package me.pixka.kt.pidevice

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import javafx.concurrent.Worker
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.Checkinrang
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.t.RunDisplaydusttoTm1
import me.pixka.kt.pidevice.worker.D1TimerII
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@DataJpaTest
class TestTimer {
    //    @Autowired
//    lateinit var mtp: MactoipService

    var mtp = mockk<MactoipService>()

    @Test
    fun Worker() {
        var pijob = Pijob()
        pijob.hlow = BigDecimal(5)
        pijob.tlow = BigDecimal(20)
        pijob.thigh = BigDecimal(30)
        var worker = D1TimerII(pijob, mtp, mockk<NotifyService>(relaxed = true))
        every { mtp.readTmp(pijob) } returns BigDecimal(20)
        var re = worker.waitlowtmp(pijob)
        Assertions.assertTrue(re)

    }

    @Test
    fun testFull() {


        var pijob = Pijob()
        pijob.hlow = BigDecimal(5)
        pijob.tlow = BigDecimal(20)
        pijob.thigh = BigDecimal(30)
        every { mtp.readTmp(pijob) } returns BigDecimal(31)


        var re = waitlowtmp(pijob)
        Assertions.assertTrue(re)

        every { mtp.readTmp(pijob) } returns BigDecimal(30)
        re = waithightmp(pijob)
        Assertions.assertTrue(re)

        verify(atLeast = 2) { mtp.readTmp(pijob) }
    }

    fun waithightmp(pijob: Pijob): Boolean {
        try {
            var waittime = 10
            if (pijob.hlow != null) {
                waittime = pijob.hlow!!.toInt()
            }
            for (i in 0..waittime) {

                var t = mtp.readTmp(pijob)
                if (t != null && t.toDouble() >= pijob.thigh!!.toDouble()) {
                    return true
                }

                TimeUnit.SECONDS.sleep(1)
            }
            throw Exception("Wait high timeout ${waittime}")
        } catch (e: Exception) {
            logger.error("waithightmp ERROR ${e.message}")
            throw e
        }

    }

    fun waitlowtmp(pijob: Pijob): Boolean {
        try {
            var waittime = 10
            if (pijob.hlow != null) {
                waittime = pijob.hlow!!.toInt()
            }
            for (i in 0..waittime) {
                var t = mtp.readTmp(pijob)
                if (t != null && (t.toDouble() >= pijob.tlow!!.toDouble() && t.toDouble() < pijob.thigh!!.toDouble()  )) {
                    return true
                }
                TimeUnit.SECONDS.sleep(1)
            }
            throw Exception("Wait low timeout ${waittime}")

        } catch (e: Exception) {
            logger.error("waitlowtmp ERROR ${e.message}")
            throw e
        }

    }

    var logger = LoggerFactory.getLogger(TestTimer::class.java)

    @Test
    fun testLow() {


        var mtp = mockk<MactoipService>()
        var line = mockk<NotifyService>(relaxed = true)
        var pijob = Pijob()
        pijob.tlow = BigDecimal(9)
        pijob.thigh = BigDecimal(10)
        pijob.runtime = 1

        var list = ArrayList<Portstatusinjob>()
        var p = Portstatusinjob()
        p.runtime = 10
        p.waittime = 0
        list.add(p)

        every { mtp.setport(p) } returns "{ok}"
        every { mtp.readTmp(pijob) } returns BigDecimal(10)
        every { mtp.getPortstatus(pijob) } returns list
        var t = D1TimerII(pijob, mtp, line)


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