package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.t.QueueService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestRemoveQ {


    @Test
    fun removeQ() {
        var job = Pijob()
        job.id = 1
        job.name = "test remove"
        job.etimes = "5:00"
        job.stimes = "6:00"

        var q = QueueService()
        Assertions.assertTrue(q.addtoqueue(job))

        Assertions.assertEquals(1,q.queue.size)
        Assertions.assertTrue(q.remove(job))
        Assertions.assertEquals(0,q.queue.size)

    }
}