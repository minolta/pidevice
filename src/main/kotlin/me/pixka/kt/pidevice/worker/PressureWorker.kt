package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
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
open class PressureWorker(p: Pijob, val mactoipService: MactoipService, var ntfs: NotifyService) : DWK(p), Runnable {
    override fun run() {

        var token = System.getProperty("pressurenotify")
        var psi: Double? = 0.0
        try {
            startRun = Date()
            status = "Start run ${startRun}"
            isRun = true

            var canrun = runWaitrang()

            if (canrun) {
                status = "Perssure in rang run set port"
                setPort()
                status= " exit normal "
            }

            exitdate = findExitdate(pijob)
//            status = "Exit job"
        } catch (e: Exception) {
            status = "Exit with ERROR ${e.message}"
            isRun = false

        }
    }

    fun setPort() {
        var ports = mactoipService.getPortstatus(pijob)

        if (ports != null) {
            ports.forEach {
                try {
                    mactoipService.setport(it)
                    if (pijob.token != null) {
                        var loop = 10
                        if (pijob.hhigh != null)
                            loop = pijob.hhigh!!.toInt()
                        for (i in 0..loop) {
                            var m = ""
                            if (pijob.description != null)
                                m = pijob.description!!
                            ntfs.message("แรงดันตำมากเกินเวลา ${pijob.tlow}  ${m}", pijob.token!!)
                            TimeUnit.SECONDS.sleep(1)
                        }
                    }
                    if (it.runtime != null)
                        TimeUnit.SECONDS.sleep(it.runtime!!.toLong())
                    if (it.waittime != null)
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
        try {
            while (true) {

                if (!checkPressure()) {
                    status = "Pressure not in rang"
//                    if (pijob.waittime != null) {
//                        var wait = pijob.waittime!!.toLong()
//                        TimeUnit.SECONDS.sleep(wait)
//                    }

//                    status = "Exit job"
                    return false

                }
                count++
                status = "Perssure in rang ${count}/${checktime}"
                TimeUnit.SECONDS.sleep(1)
                if (count >= checktime)
                    break
            }
            status = "Perssure in condition now"
            return true
        } catch (e: Exception) {
            logger.error("Check perssion ERROR ${e.message}")
            throw e
        }
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