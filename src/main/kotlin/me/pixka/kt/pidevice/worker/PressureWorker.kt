package me.pixka.kt.pidevice.worker

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * worker จะทำการตรวจสอบว่า แรงดันอยู่ในช่วงหรือเปล่า
 *
 */
class PressureWorker(p: Pijob, val mactoipService: MactoipService) : DWK(p), Runnable {
    override fun run() {

        var token = System.getProperty("pressurenotify")
        var psi: Double? = 0.0

        startRun = Date()
        status = "Start run ${startRun}"
        isRun = true

        var canrun = runWaitrang()

        if (canrun) {
            status = "Perssure in rang run set port"
            setPort()
        }

        exitdate = findExitdate(pijob)
        status = "Exit job"
    }

    fun setPort() {
        var ports = mactoipService.getPortstatus(pijob)

        if (ports != null) {
            ports.forEach {
                try {
                    mactoipService.setport(it)
                    if (it.runtime != null)
                        TimeUnit.SECONDS.sleep(it.runtime!!.toLong())
                    if(it.waittime!=null)
                        TimeUnit.SECONDS.sleep(it.waittime!!.toLong())
                } catch (e: Exception) {
                    status = "${pijob.name} set port ERROR ${e.message}"
                    logger.error("${pijob.name} Set port ERROR ${e.message}")
                }
            }
        }
    }

    fun runWaitrang(): Boolean {
        var checktime = pijob.hlow!!.toInt()
        var count = 0

        while (true) {

            if (!checkPressure()) {
                status = "Pressure not in rang"
                if (pijob.waittime != null) {
                    var wait = pijob.waittime!!.toLong()
                    TimeUnit.SECONDS.sleep(wait)
                }

                status = "Exit job"
                return false

            }
            count++
            TimeUnit.SECONDS.sleep(1)
            if (count >= checktime)
                break
        }

        return true

    }

    fun checkPressure(): Boolean {
        try {
            var p = mactoipService.readPressure(pijob.desdevice!!)
            var l = pijob.tlow!!.toDouble()
            var h = pijob.thigh!!.toDouble()

            if (p!! in l..h) {
                return true
            }
            return false
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
            throw e
        }


    }

    var logger = LoggerFactory.getLogger(PressureWorker::class.java)
}