package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import java.util.*


class D1tjobWorker(p: Pijob,
                   val readvalue: ReadUtil, val pijs: PortstatusinjobService, var test: Pijob? = null,
                   var ips: IptableServicekt,var httpService: HttpService)
    : DefaultWorker(p, null, readvalue, pijs, logger) {
    var exitdate: Date? = null
    var totalrun = 0
    var totalwait  = 0
    var om = ObjectMapper()
    fun findExitdate(pijob: Pijob): Date? {
        var t = 0L

        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!

        t = t + totalwait +totalrun -5 //ต้อง ลบ 5 มันทำงานเร็วขึ้น
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.

        calendar.add(Calendar.SECOND, t.toInt())
        var exitdate = calendar.time
        if (t == 0L)
            return null

        return exitdate
    }

    override fun run() {
        try {
            startRun = Date()
            isRun = true
            Thread.currentThread().name = "D1tjobWorker JOBID:${pijob.id}  D1T:${pijob.name} ${startrun}"
            logger.debug("D1tjobWorker ${pijob.name} Pijob ${pijob} runwith ${test}")
            if (runwith(test)!!) {
                status = "D1tjobWorker ${pijob.name} Start run this job ${pijob.id} "
                logger.debug("D1tjobWorker ${pijob.name} Now jobrun D1tjob")
                var ports = pijs.findByPijobid(pijob.id)
                logger.debug("D1tjobWorker set port ${ports} ${pijob}")
                if (ports != null) {
                    setPort(ports as List<Portstatusinjob>)
//                    setRemoteport(ports as List<Portstatusinjob>)
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


        exitdate = findExitdate(pijob)
        if (exitdate == null)
            isRun = false
        logger.debug("D1tjobWorker ${pijob.name} Run d1 T job  ")


    }

    fun setPort(ports: List<Portstatusinjob>) {

        ports.filter { it.enable == true }.forEach {

            try {
                var traget = it.device
                var ip = ips.findByMac(traget?.mac!!)
                var runtime = it.runtime
                var waittime = it.waittime

                if(runtime!=null && runtime!!>totalrun)
                    totalrun = runtime
                if(waittime!=null && waittime >totalwait)
                    totalwait  = waittime

                var portname = it.portname?.name
                var value = it.status
                var url = "http://${ip?.ip}/run?port=${portname}&value=${value?.toInt()}&delay=${runtime}&waittime=${waittime}"
                var re =  httpService.get(url)
                var s = om.readValue<Status>(re)

                status = "Set port ${portname} to ${value?.toInt()} run ${runtime} wait${waittime}"
            } catch (e: Exception) {
                status = "ERROR ${pijob.name} ${e.message}"
            }
        }
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
        logger.debug("D1tjobWorker checktmp ${pijob.name} Check temp ${p}")
        status = "${pijob.name} checktmp Check temp ${p}"
        try {
            var v = readUtil?.readTfromD1Byjob(p)
            logger.debug("${pijob.name} value from Readtmp ${v} checktmp")
            status = "${pijob.name} value from Readtmp ${v} "
            var l = p.tlow?.toDouble()
            var h = p.thigh?.toDouble()
            var now = v?.t?.toDouble()
            logger.debug("${pijob.name} temp check ${l} < ${v} < ${h} checktmp")
            status = "${pijob.name} temp check ${l} < ${v} < ${h}"
            if (l != null && h != null && now != null) {
                if (l <= now && now <= h) {
                    logger.debug("${pijob.name} In rang can run")
                    return true //in rang
                }

            }
            logger.debug("${pijob.name}  Out of rang checktmp")
            return false
        } catch (e: Exception) {
            logger.error("${pijob.name} Check temp ${e.message} checktmp")
            status = "${pijob.name} Check temp ${e.message} checktmp"
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