package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import java.util.*


class D1pjobWorker(job: Pijob, val readUtil: ReadUtil, val mtp: MactoipService)
    : DWK(job), Runnable {
    override fun run() {
        startRun = Date()
        isRun = true
        var v: PressureValue? = null
        Thread.currentThread().name = "JOBID:${pijob.id} D1P ${pijob.name} ${startRun}"
        try {
            v = readUtil.readPressureByjob(pijob)
            logger.debug("D1 Found pressure : ${v}")
            status = "D1 Found pressure : ${v}"
            if (v != null) {
                var value = v.pressurevalue?.toFloat()
                var h = pijob.hhigh?.toFloat()
                var l = pijob.hlow?.toFloat()
                logger.debug("D1 pressure  ${l} < ${value} > ${h}")
                if (checkH(l!!, h!!, value!!)) {
//                    go()
                    goII()
                    isRun = false
                } else {
                    logger.error("D1 pressure job Value not in rang ${l} < ${value} > ${h}")
                    status = "D1 pressure job Value not in rang ${l} < ${value} > ${h}"
                    isRun = false
                }
            } else {
                status = "Not found pressure value"
                isRun = false
            }
        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR ${e.message}  HLOW:${pijob.hlow} HHIGH:${pijob.hhigh}  ${v}")
            status = "ERROR ${e.message} HLOW:${pijob.hlow} HHIGH:${pijob.hhigh} ${v}"
            throw e
        }

        isRun = false

    }


    fun checkH(l: Float, h: Float, v: Float): Boolean {
        status = "Check value ${l} < ${v} > ${h}"
        if (v >= l && v <= h) {
            return true
        }
        return false
    }

    fun goII() {
        if (pijob.ports != null) {
            var ports = pijob.ports!!.filter { it.enable == true }

            ports.forEach {
                try {
                    var re = mtp.setport(it)
                    status = "Setport ${it.device?.name} port:${it.portname?.name} to ${it.status?.name}"
                } catch (e: Exception) {
                    mtp.lgs.createERROR("${e.message}", Date(),
                            "D1pjobWorker", Thread.currentThread().name, "69", "goII()",
                            "${it.device?.mac}", pijob.refid, pijob.pidevice_id)
                }
            }
            status = "End set port"
        }
    }

//    fun go() {//Run
//        status = "Run set port "
//        var ports = pijob.ports
//        logger.debug("Ports ${ports}")
//        if (ports != null)
//            for (port in ports) {
//
//
//                var pw = port.waittime
//                var pr = port.runtime
//                var pn = port.portname!!.name
//                var tg = port.device
//                var v = port.status
//
//                var portname = pn
//                var runtime = 0L
//                if (pr != null) {
//                    runtime = pr.toLong()
//                } else if (pijob.runtime != null) {
//                    runtime = pijob.runtime!!
//                }
//
//                var waittime = 0L
//                if (pw != null) {
//                    waittime = pw.toLong()
//                } else if (pijob.waittime != null) {
//                    waittime = pijob.waittime!!
//                }
//                var value = 0
//                if (v != null) {
//                    if (v.name.equals("high")) {
//                        value = 1
//                    } else value = 0
//                }
//                var ee = Executors.newSingleThreadExecutor()
//                try {
//                    var url = findUrl(tg!!, portname!!, runtime, waittime, value)
//                    logger.debug("URL ${url}")
//                    status = "Set port ${url}"
//                    var get = HttpGetTask(url)
//
//                    var f = ee.submit(get)
//                    var value = f.get(5, TimeUnit.SECONDS)
//                    status = "Delay  ${runtime} + ${waittime}"
//                    logger.debug("Value ${value}")
//                    TimeUnit.SECONDS.sleep(runtime)
//                    TimeUnit.SECONDS.sleep(waittime)
//                } catch (e: Exception) {
//                    logger.error("Error ${e.message}")
//                    status = " Error ${e.message}"
//                    ee.shutdownNow()
//                }
//
//            }
//
//        status = "Set port ok "
//
//    }


    override fun toString(): String {
        return "${pijob.name}"
    }


    var logger = LoggerFactory.getLogger(D1pjobWorker::class.java)


}