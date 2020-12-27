package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import java.util.*

class D1tjobWorker(p: Pijob, val pijs: PortstatusinjobService, val mtp: MactoipService)
    : DWK(p), Runnable {
    var totalrun = 0
    var totalwait = 0
    var om = ObjectMapper()

    //remove run with
    override fun run() {
        try {
            startRun = Date()
            isRun = true
            Thread.currentThread().name = "D1tjobWorker JOBID:${pijob.id}  D1T:${pijob.name} "
            logger.debug("D1tjobWorker ${pijob.name} Pijob ${pijob}")
            status = "D1tjobWorker ${pijob.name} Start run this job ${pijob.id} "
            logger.debug("D1tjobWorker ${pijob.name} Now jobrun D1tjob")
            var ports = pijs.findByPijobid(pijob.id)
            logger.debug("D1tjobWorker set port ${ports} ${pijob}")
            if (ports != null) {
                setPort(ports as List<Portstatusinjob>)
            } else {
                logger.debug("D1tjobWorker ${pijob.name} No port to set")
            }

        } catch (e: Exception) {
            logger.error(e.message)
            mtp.lgs.createERROR("${e.message}", Date(),
                    "D1tjobWorker", "", "43", "run",
                    pijob.desdevice?.mac, pijob.refid)
            isRun = false
            status = "D1tjobWorker ${pijob.name} error ${e.message}"
            throw e
        }


        exitdate = findExitdate(pijob)
        if (exitdate == null)
            isRun = false
        logger.debug("D1tjobWorker ${pijob.name} Run d1 T job  ")
//        status = "D1tjobWorker ${pijob.name} Run d1 T job "
    }

    fun setPort(ports: List<Portstatusinjob>) {

        ports.filter { it.enable == true }.forEach {
            try {
                var re = mtp.setport(it)
                var runtime = it.runtime
                var waittime = it.waittime
                if (runtime != null && runtime > totalrun)
                    totalrun = runtime
                if (waittime != null && waittime > totalwait)
                    totalwait = waittime
                var portname = it.portname?.name
                var value = it.status
                var s = om.readValue<Status>(re)
                status = "Set port ${portname} to ${value?.toInt()} run ${runtime} wait${waittime} Status :${s.status}"
            } catch (e: Exception) {
                mtp.lgs.createERROR("${e.message}", Date(),
                        "D1tjobWorker", "", "82", "setPort",
                        pijob.desdevice?.mac, pijob.refid)
                status = "ERROR ${pijob.name} ${e.message}"
            }
        }
    }

    //สำหรับ test job ที่ run ว่า ok มันทำงานได้เปล่าถ้าใช่ก็ run ต่อไปได้
//    fun runwith(testjob: Pijob?): Boolean? {
//        logger.debug("D1tjobWorker ${pijob.name} Start runwidth")
//        try {
//            //ถ้ามี runwidthid
//            logger.debug("D1tjobWorker ${pijob.name} runwidth Check job ${testjob}")
//            if (testjob != null) {
//                var re = readUtil?.checktmp(testjob)
//                logger.debug(" D1tjobWorker ${pijob.name} runwidth Test result ${re}")
//                return re
//            }
//            return true // ถ้าไม่กำหนด run with ก็ run เลย
//        } catch (e: Exception) {
//            lgs.createERROR("${e.message}", Date(), "D1tjobWorker",
//                    pijob.name, "runwith")
//            logger.error("D1tjobWorker ${pijob.name} Error runwith() ${e.message}")
//            throw  e
//        }
//        return false
//    }


    override fun toString(): String {
        return "${pijob.name} "
    }

    var logger = LoggerFactory.getLogger(D1tjobWorker::class.java)

}