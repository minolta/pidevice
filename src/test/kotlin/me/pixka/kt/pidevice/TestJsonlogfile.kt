package me.pixka.kt.pidevice

import me.pixka.log.d.Logsevent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class TestJsonlogfile
{

    @Test
    fun readLog()
    {
        var f = File("d.log")
        Assertions.assertTrue(f.exists())

        var l = Logsevent()
    }
}