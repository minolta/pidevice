package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Devicecheckin
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.pibase.o.Infoobj
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
class Checkin(val notifyService: NotifyService, val mtp: MactoipService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun checkin() {
        try {
            var url = System.getProperty("piserver") + "/checkin"
            val i = Infoobj()
            i.ip = System.getProperty("fixip")
            i.mac = System.getProperty("mac")
            i.password = UUID.randomUUID().toString() // สร้าง ยฟหหไนพก
            var re = mtp.http.post(url, i, 2000)
//            println(re)
            var t = om.readValue<Devicecheckin>(re)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }

    }

    internal var logger = LoggerFactory.getLogger(Checkin::class.java)
}

