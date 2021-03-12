package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import java.util.*

class OffpumpWorker(job: Pijob, val mtp: MactoipService) : DWK(job), Runnable {
    val om = ObjectMapper()
    override fun run() {
        isRun = true
        startRun = Date()
        logger.debug("Start run ${startRun}")
        var mac: String? = null
        try {
            var ip = pijob.desdevice?.ip
            mac = pijob.desdevice?.mac
            try {
                status = "Try to OFF Pump ${pijob.name}"
                var re = mtp.http.get("http://${ip}/off", 60000)
                var s = om.readValue<Status>(re)
                status = "Off pumb is ok ${s.uptime} Power is off ok."
            } catch (e: Exception) {
                isRun = false
                logger.error("Off pumb error  offpump ${e.message} ${pijob.name}")
                mtp.lgs.createERROR(" ${e.message}", Date(),
                        "OffpumpWorker", "", "", "run", mac, pijob.refid)
                status = "Off pumb error  offpump ${e.message} ${pijob.name}"

            }
        } catch (e: Exception) {
            isRun = false
            logger.error("offpump ${e.message} ${pijob.name}")
            mtp.lgs.createERROR("${e.message}", Date(), "OffpumpWorker",
                    "", "", "run", mac, pijob.refid)
            status = "offpump ${e.message} ${pijob.name}"

        }
        exitdate = findExitdate(pijob)
    }

    var logger = LoggerFactory.getLogger(OffpumpWorker::class.java)
}