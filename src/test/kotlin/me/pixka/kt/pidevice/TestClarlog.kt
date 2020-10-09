package me.pixka.kt.pidevice

import me.pixka.log.d.LogService
import me.pixka.log.d.Logsevent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestClarlog
{
    @Autowired
    lateinit var ls:LogService

    @Test
    fun TestClar()
    {

        var l = Logsevent()
        l.toserver = true
        ls.save(l)

        l = Logsevent()
        l.toserver = true
        ls.save(l)


        Assertions.assertEquals(2,ls.all().size)
        ls.delete(true)
        Assertions.assertEquals(0,ls.all().size)


    }
}