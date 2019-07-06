package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.util.*


class D1tjobWorker(p: Pijob,
                   val readvalue: ReadUtil, val pijs: PortstatusinjobService, var test: Pijob? = null)
    : DefaultWorker(p, null, readvalue, pijs, logger) {
    override fun run() {
        try {
            startrun = Date()
            startRun = Date()
            isRun = true
            Thread.currentThread().name = "D1tjobWorker JOBID:${pijob.id}  D1T:${pijob.name} ${startrun}"
            logger.debug("D1tjobWorker ${pijob.name} Pijob ${pijob} runwith ${test}")
            if (checktmp(pijob) && runwith(test)!!) {
                status = "D1tjobWorker ${pijob.name} Start run this job ${pijob.id} "
                logger.debug("D1tjobWorker ${pijob.name} Now jobrun D1tjob")
                var ports = pijs.findByPijobid(pijob.id)
                logger.debug("D1tjobWorker set port ${ports} ${pijob}")
                if (ports != null) {

                    setRemoteport(ports as List<Portstatusinjob>)

                } else {
                    logger.debug("D1tjobWorker ${pijob.name} No port to set")
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            isRun = false
            status = "D1tjobWorker ${pijob.name} error ${e.message}"
            throw e
        }

        isRun = false
        logger.debug("D1tjobWorker ${pijob.name} Run d1 T job  ")


    }

    //สำหรับ test job ที่ run ว่า ok มันทำงานได้เปล่าถ้าใช่ก็ run ต่อไปได้
    fun runwith(testjob: Pijob?): Boolean? {
        logger.debug("D1tjobWorker ${pijob.name} Start runwidth")
        try {
            //ถ้ามี runwidthid
            logger.debug("D1tjobWorker ${pijob.name} runwidth Check job ${testjob}")
            if (testjob != null) {
                var re = readUtil?.checktmp(testjob)
                logger.debug(" D1tjobWorker ${pijob.name} runwidth Test result ${re}")
                return re
            }
            return true // ถ้าไม่กำหนด run with ก็ run เลย

        } catch (e: Exception) {
            logger.error("D1tjobWorker ${pijob.name} Error runwith() ${e.message}")
            throw  e
        }
        return false
    }

    fun checktmp(p: Pijob): Boolean {
        logger.debug("D1tjobWorker ${pijob.name} Check temp ${p}")
        status = "${pijob.name} Check temp ${p}"
        try {
            var v = readUtil?.readTfromD1Byjob(p)
            logger.debug("${pijob.name} value from Readtmp ${v}")
            status = "${pijob.name} value from Readtmp ${v}"
            var l = p.tlow?.toDouble()
            var h = p.thigh?.toDouble()
            var now = v?.t?.toDouble()
            logger.debug("${pijob.name} temp check ${l} < ${v} < ${h}")
            status = "${pijob.name} temp check ${l} < ${v} < ${h}"
            if (l != null && h != null && now != null) {
                if (l <= now && now <= h) {
                    return true //in rang
                }

            }
            return false
        } catch (e: Exception) {
            logger.error("${pijob.name} Check temp ${e.message}")
            status = "${pijob.name} Check temp ${e.message}"
            throw e
        }
    }

    var startrun: Date? = null

    override fun toString(): String {
        return "${pijob.name} "
    }
    companion object {
        internal var logger = LoggerFactory.getLogger(D1tjobWorker::class.java)
    }
}