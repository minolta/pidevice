package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit



@Component
class Runsavedevice(val service: PideviceService) {

    companion object {
        internal var logger = LoggerFactory.getLogger(Runsavedevice::class.java)
    }

    val mapper = ObjectMapper()
    @Scheduled(fixedDelay = 5000)
    fun run() {
        var target = System.getProperty("piserver") + "/rest/pidevice/lists/20000/10000"
        logger.debug("URL ${target}")
        var http = HttpGetTask(target)
        val ee = Executors.newSingleThreadExecutor()
        var f = ee.submit(http)
        try {
            var re = f.get(5, TimeUnit.SECONDS)
            logger.debug("RE ${re}")
            val list = mapper.readValue<List<PiDevice>>(re!!)

            if(list != null)
            {
                for(d in list)
                {
                    var localdevice = service.findByMac(d.mac!!)
                    logger.debug("Local Device ${localdevice}")
                    if(localdevice!=null) {
                        localdevice.name = d.name
                        localdevice.description = d.description
                        service.save(localdevice)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Update device is error ${e.message}")
        }

    }
}