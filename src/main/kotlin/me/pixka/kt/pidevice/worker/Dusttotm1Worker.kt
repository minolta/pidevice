package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.o.Dustobj
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class Dusttotm1Worker(pijob: Pijob, var mtc: MactoipService) : DWK(pijob), Runnable {
    override fun run() {

        try {
            isRun = true
            startRun = Date()

            var s = mtc.readStatus(pijob)

            try {
                var d = mtc.om.readValue<Dustobj>(s)
                status = "Pm is ${d.pm25}"
                todisplay(d.pm25?.toInt()!!)
                exitdate = findExitdate(pijob)

            } catch (e: Exception) {
                logger.error("Convert dust obj error ${e.message}")
                isRun=false
            }

        } catch (e: Exception) {
            logger.error("Read dust ERROR ${e.message} JOB: ${pijob.name}")
            isRun = false
        }


    }


    fun todisplay(pm: Int) {
        var ports = mtc.getPortstatus(pijob)

        if (ports != null) {
            ports.forEach {

                try {
                    var ip = mtc.mactoip(it.device!!.mac!!)
                    var url = "http://${ip}/settm1?value=${pm}"
                    mtc.http.getNoCache(url, 1000)
                    status = "Display to ${it.device?.name} Pm2.5 ${pm}"
                } catch (e: Exception) {
                    logger.error("Set tm ERROR ${e.message} ")
                }
            }
        }
    }

    var logger = LoggerFactory.getLogger(Dusttotm1Worker::class.java)
}