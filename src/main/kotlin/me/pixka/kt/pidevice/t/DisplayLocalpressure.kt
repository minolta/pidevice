package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pidevice.s.InfoService
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Component
class DisplayLocalpressure(val dps: DisplayService, val readUtil: ReadUtil, val infoService: InfoService) {
    var df = DecimalFormat("##")
    @Scheduled(fixedDelay = 15000)
    fun display() {
        logger.debug("Start display Localhost Pressure")
        if (infoService.A0 != null) {
            try {
                var psinow = infoService.A0?.psi
                if (psinow != null) {
                    var d = dps.lockdisplay(this)
                    var v = df.format(psinow)
                    logger.debug("Psi ${v}")
                    d.clear()
                    d.print("P:" + v)
                    TimeUnit.SECONDS.sleep(10)
                    dps.unlock(this)
                }
                else
                {
                    logger.error("Psi is null")
                }
            } catch (e: Exception) {
                logger.error("Error ${e.message}")
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DisplayLocalpressure::class.java)
    }

}