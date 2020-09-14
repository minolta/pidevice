package me.pixka.kt.pidevice

import me.pixka.kt.pibase.s.FindJob
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TestGetdustjob
{
    @Autowired
    lateinit var findJob: FindJob


    @Test
    fun TestGetjob()
    {

        var job = findJob.loadjob("readdust")
        Assertions.assertEquals(1,job?.size)
    }
}