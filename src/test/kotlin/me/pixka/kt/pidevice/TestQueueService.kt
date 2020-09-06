package me.pixka.kt.pidevice

import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.t.QueueService
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.TaskServiceII
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.TimeUnit


@SpringBootTest
class TestQueueService {

    @Autowired
    lateinit var findJob: FindJob

    @Autowired
    lateinit var qs: QueueService

    @Autowired
    lateinit var pjs: PijobService

    @Autowired
    lateinit var ts: TaskService

    @Autowired
    lateinit var tsii:TaskServiceII


    @Test
    fun testQueue() {

        var r = findJob.loadjob("runhbyd1")
//        println(r)

//        r?.forEach { println(it) }

        Assertions.assertEquals(10,r?.size)

        r?.forEach { qs.addtoqueue(it) }
        Assertions.assertEquals(10,qs.queue.size)

//        TimeUnit.SECONDS.sleep(5)

//        r?.forEach{qs.addtoqueue(it)}
//        Assertions.assertEquals(20,qs.queue.size)


        qs.peek()
        Assertions.assertEquals(10,qs.queue.size)
        qs.poll()
        Assertions.assertEquals(9,qs.queue.size)

        println(qs.queue.size)

    }


}