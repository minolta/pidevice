package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.ErrorlogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("pi")
class Execdhtcommand() {
    companion object {
        internal var logger = LoggerFactory.getLogger(Execdhtcommand::class.java)
    }

    @Scheduled(initialDelay = 10000,fixedDelay = 15000)
    fun c() {
        logger.info("Run dht.py ${Date()}")
        try {
            val proc = Runtime.getRuntime().exec("/root/run")

        }catch (e:Exception)
        {
            logger.error("${e.message}")
            //err.n("Execdhtcommand","24",e.message!!)
        }
    }

}