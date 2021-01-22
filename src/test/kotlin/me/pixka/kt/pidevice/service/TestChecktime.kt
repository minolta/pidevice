package me.pixka.kt.pidevice.service

import me.pixka.kt.pibase.d.Pijob
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.*

class TestChecktime {
    var df = SimpleDateFormat("HH:mm")
    var d = SimpleDateFormat("dd/MM/yyyy HH:mm")
    fun checkTime(job: Pijob, now: Date): Boolean {
        try {

            if (job.stimes.isNullOrEmpty() && job.etimes.isNullOrEmpty())
                return true // not set time rang
            var n = df.parse(df.format(now))
            var nl = n.time

            //หาว่าเวลาก่อนหน้าที่กำหนดหรือเปล่า
            if (job.stimes.isNullOrEmpty() && job.etimes!=null && job.etimes!!.isNotEmpty()) {
                var e = df.parse(job.etimes)
                var el = e.time

                if (nl <= el)
                    return true

                return false
            }

            if(job.stimes!=null && job.etimes.isNullOrEmpty() && job.stimes!!.isNotEmpty())
            {
                var e = df.parse(job.etimes)
                var el = e.time

                if (nl >= el)
                    return true

                return false
            }
            //ทดสอบแบบก่อนเวลา


        } catch (e: Exception) {

        }

        return false
    }

    @Test
    fun testChecktimefunction() {

        var job = Pijob()
        job.stimes = "10:10"
        job.etimes = null

        var testdate = d.parse("10/10/2020 12:30")

        var can = checkTime(job, testdate)
        Assertions.assertTrue(can)
    }
}