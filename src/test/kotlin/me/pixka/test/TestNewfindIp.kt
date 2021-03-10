package me.pixka.test

import me.pixka.kt.pidevice.t.NewFindip
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit

@DataJpaTest
@Profile("ip")
class TestNewfindIp {


    @Autowired
    lateinit var nf:NewFindip


    @Test
    fun testFindnewIP()
    {

        Assertions.assertNotNull(nf)

//        TimeUnit.SECONDS.sleep(10)
    }

}