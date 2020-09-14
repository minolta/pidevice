package me.pixka.kt.pidevice

import me.pixka.kt.pibase.o.HObject
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

class TestRunHjob {

    @Test
    fun testRunHjob() {
        CompletableFuture.supplyAsync { //อ่าน H
            println("State 1")
            var h = HObject()
//            throw Exception("test")
            h.h = BigDecimal(0.0)
            h

        }.thenApply {
            println("Check run")
//            throw Exception("test1")

            it
//            checkHtorun(job, it)

        }.thenApply {
            println(it)
            throw Exception("test3")
            it

        }.exceptionally {
            println(it.message)
            HObject()
        }
    }
}