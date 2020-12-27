package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * ใช้ทดสอบ ระบบ off pump ว่าทำงานตรงหรือเปล่า
 */
class TestTimeoffpumb {
    var df = SimpleDateFormat("HH:mm")
    fun check(job: Pijob, now: Date): Boolean {
        var can = false
        try {
            var n = df.format(now)
            var now = df.parse(n)
            logger.debug("checktime  ${job.name} : N:${n} now ${now} now time ${now.time}")
            logger.debug("checktime  ${job.name} : s: ${job.stimes} ${now} e:${job.etimes}")
            if (job.stimes != null && job.etimes != null) {
                var st = df.parse(job.stimes).time
                var et = df.parse(job.etimes).time
                if (st <= now.time && now.time <= et)
                    can = true
                logger.debug("checktime  ${job.name} : ${st} <= ${now} <= ${et} can:${can}")

            } else if (job.stimes != null && job.etimes == null) {
                var st = df.parse(job.stimes).time
                if (st <= now.time)
                    can = true
                logger.debug("checktime ${job.name} : ${st} <= ${now} Can:${can}")
            } else if (job.stimes == null && job.etimes != null) {
                var st = df.parse(job.etimes).time
                if (st <= now.time)
                    can = true
                logger.debug("checktime ${job.name} : ${st} >= ${now} can:${can}")
            } else {
                logger.debug("${job.name} checktime not set ")
                can = true
            }
        } catch (e: Exception) {
            logger.error("checktime  ${job.name} : ${e.message}")
        }

        return can
    }
     var logger = LoggerFactory.getLogger(TestTimeoffpumb::class.java)
    fun checkTime(job: Pijob, now: Date): Boolean {
        //set ช่วงเวลา
        if (job.stimes != null && job.etimes != null) {
            var s = df.parse(job.stimes)
            var e = df.parse(job.etimes)
            var n = df.parse(df.format(now))

            if (s.time <= n.time && n.time <= e.time)
                return true
        }
        //แบบถ้าเวลาที่ส่งมาหลังจากนี้จะทำงาน
        if (job.stimes != null && job.etimes == null) {
            var s = df.parse(job.stimes)
            var n = df.parse(df.format(now))
            if (s.time <= n.time)
                return true
        }
        //ทดสอบแบบก่อนเวลา
        if (job.stimes == null && job.etimes != null) {
            var e = df.parse(job.etimes)
            var n = df.parse(df.format(now))
            if (n.time <= e.time)
                return true
        }
        return false
    }

    @Test
    fun testInTime() {

        var job = Pijob()
        job.stimes = df.format(df.parse("1:00"))
        job.etimes = df.format(df.parse("5:00"))

        var now = df.parse("0:50")
        var r = checkTime(job, now)
        Assertions.assertTrue(r)
    }

    @Test
    fun testAfterTime() {
        var job = Pijob()
        job.stimes = df.format(df.parse("0:30"))
//        job.etimes = df.format(df.parse("5:00"))

        var now = df.parse("0:40")
        var r = checkTime(job, now)
        Assertions.assertTrue(r)
    }

    @Test
    fun testBerforeTime() {
        var job = Pijob()
        job.etimes = df.format(df.parse("0:30"))
//        job.etimes = df.format(df.parse("5:00"))

        var now = df.parse("0:20")
        var r = checkTime(job, now)
        Assertions.assertTrue(r)
    }
}