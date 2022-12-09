package me.pixka.kt.pidevice

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.TimeUnit

class TEstWebClient {

    @Test
    fun getCall() {
//        var webClient = WebClient.create()
//
//        var re = webClient.get().uri("http://localhost:8080/run").retrieve().bodyToMono(String::class.java)
//
//        re.onErrorResume {
//            println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEE"+it)
//            throw it
//        }
//        re.subscribe {
//            println("================= ${it}")
//        }


        TimeUnit.SECONDS.sleep(10)
    }
}