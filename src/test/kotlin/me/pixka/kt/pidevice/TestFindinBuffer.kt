package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Pijob
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestFindinBuffer {


    var buffer = ArrayList<Pijob>()


    @Test
    fun Testfliter()
    {

        var p = Pijob()
        p.id = 1

        buffer.add(p)

        p = Pijob()
        p.id=2

        var p3 = Pijob()
        p3.id=3
        buffer.add(p3)
        buffer.add(p)
        Assertions.assertEquals(3,buffer.size)
        Assertions.assertNotNull(buffer.find { it.id == 1L })

        buffer.remove(p3)
        Assertions.assertNull(buffer.find { it.id == 3L })


    }
}