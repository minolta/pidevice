package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.ReadStatusService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

/**
 * สำหรับ notify worker
 */
class NotifyPressureWorker(
    p: Pijob, var readStatusService: ReadStatusService,
    var target: String, var notify: NotifyService
) : DWK(p), Runnable {
    var token = System.getProperty("pressurenotify")
    var psi: Double? = 0.0


    override fun run() {
        try {
            startRun = Date()
            isRun = true
            status = "Status run Read psi at ${target} ${startRun}"
            psi = readStatusService.readPSI(target, 60)
            if (psi != null) {
                var low = pijob.tlow?.toDouble()
                var high = pijob.thigh?.toDouble()

                if (low!! <= psi!! && psi!! <= high!!) {
                    status = "In Rang ${low}  <= ${psi} => ${high}"
                    if (token == null)
                        token = pijob.token
                    //in rang
                    var d: String? = ""
                    if (pijob.description != null)
                        d = pijob.description
                    notify.message(
                        "Pressure In Rang ${low}  <= ${psi} => ${high} JOB:${pijob.name} description:${d}",
                        token
                    )
                } else {
                    status = "Not In Rang ${low}  <= ${psi} => ${high}"
                    logger.debug("Not in rang")
                }

            }
        } catch (e: Exception) {
            logger.error("JOB ${pijob.name} Notify Pressure ERROR ${e.message}")
            status = "Job has error ${e.message}"
            isRun = false
        }

        exitdate = findExitdate(pijob)

        status = "End job"
    }

    fun psi(): Double? {
        return psi
    }

    var logger = LoggerFactory.getLogger(NotifyPressureWorker::class.java)
}