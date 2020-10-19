package me.pixka.kt.pidevice

import me.pixka.kt.pibase.s.HttpService
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class TestSupply {

    fun test(): String {
        TimeUnit.SECONDS.sleep(1)
        return "Ok"
    }

    @Test
    fun TestThread() {

        var http = HttpService()

        CompletableFuture.supplyAsync {
            var re = http.get("http://192.168.89.123/", 500)
            re

        }.thenAccept {
            print("Result:")
            println(it)
        }.exceptionally {

            println(it)
            null
        }

        TimeUnit.SECONDS.sleep(2)
    }
}