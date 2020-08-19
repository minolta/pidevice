package me.pixka.kt.pidevice

import me.pixka.kt.pibase.t.HttpGetTask
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class TestClientview
{



    @Test
    fun test()
    {
        var ee = Executors.newSingleThreadExecutor()
        var get = HttpGetTask("http://192.168.89.211/")
        var f = ee.submit(get)
        var re = f.get(10, TimeUnit.SECONDS)
    }
}