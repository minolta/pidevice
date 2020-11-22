package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Vbatt
import me.pixka.kt.pibase.d.VbattService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestSendVbatt
{

    @Autowired
    lateinit var vs:VbattService


    @Test
    fun delete()
    {
        var v = Vbatt()
        v = vs.save(v)

        Assertions.assertEquals(1,vs.all().size)

        vs.delete(v)
        Assertions.assertEquals(0,vs.all().size)


    }

}