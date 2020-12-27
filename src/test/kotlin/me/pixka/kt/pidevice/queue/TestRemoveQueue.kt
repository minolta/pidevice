package me.pixka.kt.pidevice.queue

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.t.QueueService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestRemoveQueue {

    @Test
    fun testRun() {
        var queue = QueueService()

        var p = Pijob()
        p.id = 1
        queue.addtoqueue(p)


        Assertions.assertEquals(1, queue.size())
        var p1 = Pijob()
        p1.id = 2
        queue.addtoqueue(p1)


        var removep1 = queue.remove(p)
        Assertions.assertEquals(1, queue.size())
        Assertions.assertEquals(true, removep1)
        var p3 = Pijob()
        p3.id = 3

        var removep3 = queue.remove(p3)
        Assertions.assertEquals(false, removep3)

    }
}