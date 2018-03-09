package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.pi.io.Dotmatrix
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File
import java.util.concurrent.TimeUnit

@RestController
@Profile("pi")
class NameControl(val dps: DisplayService, val io: Piio) {

    val version = "1.0.3"
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
    @RequestMapping(value = "/showname", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun showname() {


        logger.debug("Run Display status")

        try {

            if (!dps.lock) {
                var dot = lockdisplay()
                if (dot != null) {
                    var a = io.wifiIpAddress()
                    var ip = io.wifiMacAddress()
                    dot.showMessage("Name : ${a}  IP: ${ip}  version : ${version}")
                    unlock()
                    TimeUnit.MILLISECONDS.sleep(500)
                    dot = lockdisplay()
                }
                //  TimeUnit.SECONDS.sleep(1)
            } else {
                logger.error("Time out")
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message}")
        }

    }

    fun lockdisplay(): Dotmatrix? {
        var count = 0
        while (dps.lock) {
            TimeUnit.MILLISECONDS.sleep(200)
            count++
            if (count > 100) {
                logger.error("Error Display timeout")
                return null
            }
        }
        return dps.lockdisplay(this)
    }

    fun unlock() {
        dps.unlock(this)
    }


    @CrossOrigin
    @RequestMapping(value = "/version", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun version(): String? {
        return version
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(NameControl::class.java)
    }
}