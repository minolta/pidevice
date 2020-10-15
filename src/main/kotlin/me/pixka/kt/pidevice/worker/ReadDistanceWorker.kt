package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Distance
import me.pixka.kt.pibase.d.DistanceService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.o.DistanceObj
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class ReadDistanceWorker(job: Pijob, var mtp: MactoipService, var ds: DistanceService,
                         val pideviceService: PideviceService) : DWK(job), Runnable {
    override fun run() {
        isRun = true
        startRun = Date()
        var mac: String? = null
        var jobid: Long = 0L
        try {
            mac = pijob.desdevice?.mac
            if (mac != null) {
                var ip = mtp.mactoip(mac)

                if (ip != null) {
                    var re = mtp.http.get("http://${ip}", 5000)
                    status = "Read Distance from ${pijob.desdevice?.name}"
                    var d = mtp.om.readValue<DistanceObj>(re)
                    var distance = Distance()
                    distance.pidevice = pijob.desdevice
                    distance.valuedate = Date()
                    distance.distancevalue = d.distance
                    ds.save(distance)
                    status = "End read ${d.distance}"
                    exitdate = findExitdate(pijob)
                }
            }
        } catch (e: Exception) {
            mtp.lgs.createERROR(
                    "${e.message}", Date(),
                    "ReadDistanceWorker", "", "13", "run()",mac,pijob.refid
            )
            logger.error(e.message)
            isRun=false
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDistanceWorker::class.java)
    }
}

