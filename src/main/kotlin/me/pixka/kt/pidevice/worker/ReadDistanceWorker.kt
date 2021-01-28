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

class ReadDistanceWorker(
    job: Pijob, var mtp: MactoipService, var ds: DistanceService,
    val pideviceService: PideviceService
) : DWK(job), Runnable {
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
                    var re = mtp.http.get("http://${ip}", 60000)
                    status = "Read Distance from ${pijob.desdevice?.name}"
                    var d = mtp.om.readValue<DistanceObj>(re)
                    var distance = Distance()
                    distance.pidevice = pijob.desdevice
                    distance.valuedate = Date()
                    distance.distancevalue = d.distance
                    if (d.distance!!.toInt() > 0)
                        ds.save(distance)

                    status = "End read ${d.distance}"
                    exitdate = findExitdate(pijob)
                } else {
                    logger.error("Error Read No ip ${pijob.name}")
                    mtp.lgs.createERROR(
                        "No ip ", Date(),
                        "ReadDistanceWorker", "", "26", "run()", mac, pijob.refid, pijob.pidevice_id
                    )
                    isRun = false

                }
            } else {
                logger.error("Error Read No Mac address ${pijob.name}")

                mtp.lgs.createERROR(
                    "No mac", Date(),
                    "ReadDistanceWorker", "", "23", "run()", mac, pijob.refid, pijob.pidevice_id
                )
                isRun = false

            }
        } catch (e: Exception) {
            logger.error(" Distance READ Time out ${pijob.name}  ERROR:${e.message}")
            mtp.lgs.createERROR(
                "${e.message}", Date(),
                "ReadDistanceWorker", "", "13", "run()", mac, pijob.refid
            )

            isRun = false
        }
    }

         var logger = LoggerFactory.getLogger(ReadDistanceWorker::class.java)
}

