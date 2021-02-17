package me.pixka.kt.pidevice

import me.pixka.kt.pibase.s.HttpService
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class TestESP32Sensor {


    @Test
    fun TestCall() {
        val http = HttpService()
        for (k in 0..10) {
            for (i in 0..10) {
                CompletableFuture.supplyAsync {
                    var re = http.getNoCache("http://192.168.88.14", 4000)

//                    var re = http.getNoCache("http://192.168.89.12", 4000)
//
//                println(re)
                    i
                }.thenAccept {
                    println("END : ${it} " + Date() + " ")
                }.exceptionally {
                    println("ERROR:  ${it.message}")
                    null
                }
            }
            println(http.caches.size)
            TimeUnit.SECONDS.sleep(2)
        }


        TimeUnit.SECONDS.sleep(2)
    }


}