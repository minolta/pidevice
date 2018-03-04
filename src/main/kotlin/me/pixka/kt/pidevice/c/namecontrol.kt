package me.pixka.kt.pidevice.c

import me.pixka.pibase.d.DS18value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File

@RestController
@Profile("pi")

class NameControl() {
    @CrossOrigin
    @RequestMapping(value = "/name", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): String? {
        val bufferedReader: BufferedReader = File("/home/pi/name").bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        return inputString
    }

    @CrossOrigin
    @RequestMapping(value = "/version", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun version(): String? {
        return "1.0.1-STABLE"
    }
}