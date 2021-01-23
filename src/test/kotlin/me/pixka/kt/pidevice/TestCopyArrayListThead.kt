package me.pixka.kt.pidevice

import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit


class TestCopyArrayListThead {


    @Test
    fun testCopy() {
        val list = CopyOnWriteArrayList<String>()

        var i = list.iterator()
        list.add("1")
        list.add("2")
        list.add("3")

        CompletableFuture.supplyAsync {
            TimeUnit.SECONDS.sleep(1)
            list.remove("3")
            println("Remove 3")
            TimeUnit.SECONDS.sleep(2)
        }

        CompletableFuture.supplyAsync {
//            TimeUnit.SECONDS.sleep(5)
            var i = list.iterator()
            i.forEach {
                println(it)
                TimeUnit.SECONDS.sleep(1)
            }
        }



//        TimeUnit.SECONDS.sleep(5)

        list.forEach { println(it) }
    }

}