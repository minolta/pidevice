package me.pixka.kt.pidevice

import me.pixka.kt.pidevice.s.CheckTimeService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.text.SimpleDateFormat

class TestTimeService {

    var df = SimpleDateFormat("HH:mm")

    @Autowired
    lateinit var ct:CheckTimeService

    @Test
    fun testCheckTimeService() {

        var d = df.parse("2:00")
        println(d)
        Assertions.assertTrue(ct.checkTime("2:00", "23:00", d, "test before"))
    }

    /**
     * ใช้สำหรับ ทดสอบว่า เวลาที่ส่งเข้าไปให้  อยู่หลังจาก เวลาเริ่มหรือเปล่าถ้าใช่ true
     */
    @Test
    fun testCheckTimeAfter() {
        var d = df.parse("8:00")
        println(d)
        Assertions.assertTrue(ct.checkTime("2:00", null, d, "test after"))
    }

    @Test
    fun testCheckTimeBefore() {
        var d = df.parse("0:00")
        println(d)
        Assertions.assertTrue(ct.checkTime(null, "8:00", d, "test after"))

    }
}