package me.pixka.kt.pidevice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class TestThread ()
{

    @Test
    fun testThread()
    {
        val executor: ThreadPoolExecutor = Executors.newFixedThreadPool(2) as ThreadPoolExecutor
        executor.submit({
            Thread.sleep(1000)
            null
        })
        executor.submit({
            Thread.sleep(1000)
            null
        })
        executor.submit({
            Thread.sleep(1000)
            null
        })

        assertEquals(2, executor.getPoolSize())
        assertEquals(1, executor.getQueue().size)

    }
    @Test
    fun testThreadPool()
    {
        val threadpool = ThreadPoolExecutor(5, 100, 1,
                TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>(200),
                ThreadPoolExecutor.CallerRunsPolicy())


        for(i in 0..500)
        {
            threadpool.submit({
                Thread.sleep(5000)
                null
            })
        }

        println(threadpool)
        assertEquals(4, threadpool.getPoolSize())
        assertEquals(1, threadpool.getQueue().size)

    }
}