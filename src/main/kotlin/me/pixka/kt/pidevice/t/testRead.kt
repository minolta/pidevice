package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Component
class TestRead()
{
    val om = ObjectMapper()
    @Scheduled(fixedDelay = 1000)
    fun testRead()
    {
        var get = HttpGetTask("http://192.168.88.249/ds18valuebysensor/28-0517c1eca7ff")
        var t = Executors.newSingleThreadExecutor()
        var f = t.submit(get)
        logger.debug("Furture ${f}")
        var value = f.get()
        logger.debug("GET VALIE [${value}]")
        val v = om.readValue<DS18value>(value, DS18value::class.java)
        logger.debug("V ${v}")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TestRead::class.java)
    }
}