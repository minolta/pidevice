package me.pixka.kt.pidevice.`fun`

import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.TimeUnit

class TestDefault {

    fun testdelay(d:Long=2)
    {

        println(Date())
        TimeUnit.SECONDS.sleep(d)
        println(Date())
    }

    @Test
    fun testDefular()
    {
        testdelay(1)
    }
}