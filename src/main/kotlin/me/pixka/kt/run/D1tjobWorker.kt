package me.pixka.kt.run

import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class D1tjobWorker(p: Pijob,
                   val readvalue: ReadUtil, val pijs: PortstatusinjobService)
    : DefaultWorker(p, null, readvalue, pijs, logger) {
    override fun run() {
        try {
            startrun = Date()
            isRun = true
            status = "Start run this job ${pijob}"

            if (checktmp(pijob)) {
                logger.debug("Now jobrun D1tjob")
                var ports = pijs.findByPijobid(pijob.id)
                if (ports != null) {

                    setRemoteport(ports as List<Portstatusinjob>)

                } else {
                    logger.debug("No port to set")
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)

        }

        isRun = false
        logger.debug("Run d1 T job ")


    }

    fun checktmp(p: Pijob): Boolean {
        logger.debug("Check temp ${p}")
        try {
            var v = readUtil.readTmpByjob(p)
            logger.debug("value from Readtmp ${v}")
            var l = p.tlow?.toDouble()
            var h = p.thigh?.toDouble()
            var now = v?.toDouble()
            logger.debug("temp check ${l} < ${v} < ${h}")
            if (l != null && h != null && now != null) {
                if (l <= now && now <= h) {
                    return true //in rang
                }

            }
            return false
        } catch (e: Exception) {
            logger.error("Check temp ${e.message}")
            throw e
        }
    }

    var startrun: Date? = null


    companion object {
        internal var logger = LoggerFactory.getLogger(D1tjobWorker::class.java)
    }
}