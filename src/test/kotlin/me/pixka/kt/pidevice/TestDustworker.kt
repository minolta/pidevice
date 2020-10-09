package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.t.HttpGetTask
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Supplier


class TestDustworker {

    @Test
    fun testWorker() {
//        addJob()
//
//        var job = Pijob()
//        job.tlow = BigDecimal(10)
//        job.thigh = BigDecimal(50)
//
//        var ports = ArrayList<Portstatusinjob>()
//        var port = Portstatusinjob()
//        var device = PiDevice()
//        device.ip = "192.168.89.98"
//        port.device = device
//        ports.add(port)
//        job.desdevice = device
////        var jobworker = Dustworker(job, ports)
//        var ee = Executors.newSingleThreadExecutor()
//        var f = ee.submit(jobworker)
//        var re = f.get()
//
//        println("Return ${re}")
//        Assertions.assertEquals(true, f.isDone)

    }

    val om = ObjectMapper()
    fun readDust(ip: String): Future<Pm>? {
        val completableFuture = CompletableFuture<Pm>()
        try {

            val http = HttpGetTask(ip)
            var f = Executors.newCachedThreadPool().submit(http)
            var re = f.get(2, TimeUnit.SECONDS)
//        var re = f.get()
//        TimeUnit.SECONDS.sleep(10)
            var pm = om.readValue<Pm>(re!!)
            completableFuture.complete(pm)
            return completableFuture
        } catch (e: Exception) {
            completableFuture.complete(null)
        }
        return completableFuture
    }

    @Throws(InterruptedException::class)
    fun calculateAsync(): Future<String>? {
        val completableFuture = CompletableFuture<String>()
        Executors.newCachedThreadPool().submit<Any?> {
            Thread.sleep(500)
            completableFuture.complete("Hello")
            null
        }
        return completableFuture
    }

    @Test
    fun testCompleter() {
        val completableFuture = readDust("http://192.168.89.98")
        val c1 = readDust("http://192.168.89.1")
        val result = completableFuture!!.get(3, TimeUnit.SECONDS)
        val r = c1?.get()
        println("RRRRRRRRRRRRR ${r}")
        assertTrue(result?.pm25 != null)
        assertTrue { r?.pm25 == null }
        print(result)

//        assertEquals("Hello", result)
    }

    fun f(): String {
        println(Thread.currentThread().name)
        println("Call F")
        TimeUnit.SECONDS.sleep(2)
        return "return from f"
    }

    @Test
    fun testComplete() {

        CompletableFuture.supplyAsync(Supplier { f() }).thenApply {
            println("Print in accept ${it}")
            println(Thread.currentThread().name)
        }
        CompletableFuture.supplyAsync(Supplier { f() }).thenApply {
            println("Print in accept ${it}")
            println(Thread.currentThread().name)
        }
        TimeUnit.SECONDS.sleep(10)
    }
}