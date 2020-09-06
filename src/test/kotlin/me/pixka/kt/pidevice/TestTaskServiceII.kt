package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.TaskServiceII
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.TimeUnit

//@SpringBootTest
class TestTaskServiceII {
    @Autowired
    lateinit var ts: TaskServiceII

    @Test
    fun TestTaskServiceII() {
        ts = TaskServiceII()
        var job1 = Dummyjob(1L, true, 6)
        var job2 = Dummyjob(2L, true, 2)

        ts.run(job1)
        ts.run(job2)
        ts.run(Dummyjob(3L, true, 6))

        assertEquals(3,ts.activeCount())


//        assertTrue(ts.checkrun(Dummyjob(3L, true, 3)))
        Assertions.assertEquals(3, ts.runinglist.size)
        TimeUnit.SECONDS.sleep(5)
//        assertEquals(1,ts.activeCount())
        assertEquals(2,ts.removeEndjob().size)
//        Assertions.assertEquals(1, ts.active())
        Assertions.assertEquals(2, ts.activeCount())
    }


}

class Dummyjob(var id: Long, var isRun: Boolean, var delay: Long) : PijobrunInterface, Runnable {
    override fun setP(pijob: Pijob) {
        TODO("Not yet implemented")
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        return id

    }

    override fun getPJ(): Pijob {
        TODO("Not yet implemented")
    }

    override fun startRun(): Date? {
        TODO("Not yet implemented")
    }

    override fun state(): String? {
        TODO("Not yet implemented")
    }

    override fun setrun(p: Boolean) {
        TODO("Not yet implemented")
    }

    override fun run() {
        println("Start run " + Date() + Thread.currentThread().name)
        TimeUnit.SECONDS.sleep(delay)
        isRun = false
        println("End " + Thread.currentThread().name)

    }
//
//    override fun call(): String {
//
//        TimeUnit.SECONDS.sleep(delay)
//        isRun = false
//        println("End " + Thread.currentThread().name)
//        return Thread.currentThread().name
//    }
}