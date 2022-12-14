package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.o.Dustobj
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

/**
 * check notify of dust
 */
class DustcheckWorker(job: Pijob, var mtp: MactoipService, val ntfs: NotifyService) : DWK(job), Runnable {
    var token = System.getProperty("errortoken")
    override fun run() {
        if (pijob.token != null)
            token = pijob.token
        isRun = true
        startRun = Date()
        try {
            var dust = getDustinfo()
            checkdust(dust)
            exitdate = findExitdate(pijob)
        } catch (e: Exception) {
            status = "DustcheckWorkerr ERROR ${e.message}"
            logger.error("DustcheckWorkerr ERROR ${e.message}")
            isRun = false
            throw e
        }


    }

    fun checkdust(dust: Dustobj): Boolean {
        try {
            var pm25: Double? = null
            var pm1: Double? = null
            var pm10: Double? = null

            var checkpm25: Double? = null
            var checkpm1: Double? = null
            var checkpm10: Double? = null


            if (pijob.tlow != null) {
                checkpm25 = pijob.tlow?.toDouble()
            }
            if (pijob.thigh != null) {
                checkpm10 = pijob.thigh?.toDouble()
            }
            if (pijob.hlow != null) {
                checkpm1 = pijob.hlow?.toDouble()
            }

            if (dust.pm25 != null) {
                pm25 = dust.pm25?.toDouble()
            }
            if (dust.pm1 != null)
                pm1 = dust.pm1?.toDouble()

            if (dust.pm10 != null) {
                pm10 = dust.pm10?.toDouble()
            }
            if (pm25 != null && checkpm25 != null && pm25 >= checkpm25) {
                status = "PM 2.5(${pm25}) >  ${checkpm25} "
                if (token != null) {
                    ntfs.message("Pm2.5 ${checkpm25} < ${pm25} ", token)
                }
                return true
            }
            if (pm1 != null && checkpm1 != null && pm1 >= checkpm1) {
                if (token != null) {
                    ntfs.message("Pm 1 ${checkpm1} < ${pm1} ", token)
                }
                status = "PM 1(${pm1}) >  ${checkpm1} "

                return true
            }
            if (pm10 != null && checkpm10 != null && pm10 >= checkpm10) {
                if (token != null) {
                    ntfs.message("Pm 10  ${checkpm10} < ${pm10} ", token)
                }
                status = "PM 10(${pm10}) >  ${checkpm10} "

                return true
            }
            return false
        } catch (e: Exception) {
            logger.error("checkdust ${e.message}")
            throw e
        }
    }

    fun getDustinfo(): Dustobj {
        status = "Check dust ${pijob.desdevice?.name} "
        try {
            var t = mtp.readStatus(pijob)
            return mtp.om.readValue<Dustobj>(t)
        } catch (e: Exception) {
            logger.error("Get dustinfo ${e.message}")
            throw e
        }

    }

    val logger = LoggerFactory.getLogger(DustcheckWorker::class.java)
}