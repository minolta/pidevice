package me.pixka.kt.pidevice

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class TestThreadPool

{

    @Test
    fun TestThreadPool()
    {
        var t = Executors.newFixedThreadPool(5)

        for(i in 0..10)
        {
            t.submit({
                println(Thread.currentThread().name+" Start ${Date()}")
                TimeUnit.SECONDS.sleep(5)
                println(Thread.currentThread().name+" End")
            })
        }


        TimeUnit.SECONDS.sleep(30)
        var tt = t as ThreadPoolExecutor
        Assertions.assertEquals(0,tt.activeCount)

    }
}