package me.pixka.kt.pidevice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class PideviceApplication

fun main(args: Array<String>) {
    SpringApplication.run(PideviceApplication::class.java, *args)
}
