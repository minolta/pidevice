package me.pixka.kt.pidevice.task

import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

/**
 * การ test อันนี้ต้องเปลียนเวลาให้ตรงกับเวลาทดสอบจริง
 */
//@SpringBootTest
class TestcheckTime {

//    @Autowired
//    lateinit var taskService: TaskService
    @Test
    fun testRun() {

        var cts = spyk<CheckTimeService>()
        var job = Pijob()
        job.stimes = null
        job.etimes = null
        var t = cts.checkTime(job, Date())

        Assertions.assertTrue(t)
    }

}