package me.pixka.kt.pidevice

import org.junit.jupiter.api.Test
import java.util.*

class TestExitdate
{

    @Test
    fun testExitdate()
    {
        var d = Date(Date().time+10800*60*60)
        println(d)
    }
}