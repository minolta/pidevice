package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Logistate
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class D1hjobWorker(var job: Pijob,
                   val mtp: MactoipService, val ntfs: NotifyService)
    : DWK(job), Runnable {
    var om = ObjectMapper()
    var waitstatus = false


    override fun run() {
        startRun = Date()
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        Thread.currentThread().name = "JOBID:${pijob.id} D1H : ${pijob.name} "
        try {
            if (pijob.tlow != null) {//delay ก่อน
                TimeUnit.SECONDS.sleep(pijob.tlow!!.toLong())
                logger.debug("Slow start ${pijob.tlow}")
            }
            waitstatus = false //ใช้น้ำ
            go()
            waitstatus = true //หยุดใช้น้ำแล้ว
            exitdate = findExitdate(pijob)
            if (exitdate == null)
                isRun = false
            status = "End normal"
            return
        } catch (e: Exception) {
            isRun = false
            exitdate = findExitdate(pijob)
            logger.error("ERROR 1 ${e.message}")
            status = "ERROR 1 ${e.message}"
            mtp.lgs.createERROR("ERROR 1 ${e.message}", Date(), "D1hjobWorker",
                    "", "", "", "", pijob.refid)
            waitstatus = true
            throw e
        }

    }


    fun getLogic(v: Logistate?): Int {
        var value = 0
        if (v != null && v.name != null) {
            if (v.name.equals("high") || v.name?.indexOf("1") != -1) {
                value = 1
            } else
                value = 0
        } else {
            if (v != null)
                status = "Value to set ERROR ${v.name}"
            else
                status = "Port state Error is Null"

        }

        return value
    }

    fun go() {//Run

        var token = pijob.token
        status = "Run set port "
        var ports = pijob.ports
        if (ports == null)
            return
        logger.debug("Ports ${ports}")
        ports = ports.filter { it.enable == true }

        for (port in ports) {
            if (port.enable == null || !port.enable!!) {
                logger.debug("Port disable ${port}")
                continue //ข้ามไปเลย
            }
            var pw = port.waittime
            var pr = port.runtime
            var pn = port.portname!!.name
            var v = port.status

            var portname = pn
            var runtime = 0L
            if (pr != null) {
                runtime = pr.toLong()
            } else if (pijob.runtime != null) {
                runtime = pijob.runtime!!
            }
            var waittime = 0L
            if (pw != null) {
                waittime = pw.toLong()
            } else if (pijob.waittime != null) {
                waittime = pijob.waittime!!
            }
            var value = getLogic(v)
            try {
                startRun = Date()
                try {
                    //30 วิถ้าติดต่อไม่ได้ให้หยุดเลย
                    var v = mtp.setport(port)
                    status = "Delay  ${runtime} + ${waittime}"
                    var s = om.readValue<Status>(v)
                    logger.debug("D1h Value ${status}")
                    status = "Set ${portname} to ${value}  ${s.status} and run ${runtime}"
                    TimeUnit.SECONDS.sleep(runtime)
                    if (waittime != null) {
                        status = "Wait time of port ${waittime}"
                        TimeUnit.SECONDS.sleep(waittime)
                    }
                } catch (e: ConnectException) {
                    haveError(token, e)
                } catch (e: TimeoutException) {
                    haveError(token, e)
                }
            } catch (e: Exception) {
                logger.error("Error 2 ${e.message}")
                status = " Error 2 ${e.message}"
                mtp.lgs.createERROR(" Error 2 ${e.message}", Date(), "D1hjobWorker",
                        "", "", "go()", pijob.desdevice?.mac, pijob.refid)
                isRun = false
                waitstatus = true //หยุดใช้น้ำแล้ว
                break //ออกเลย

            }
        } //end for

    }

    fun haveError(token: String?, e: Exception) {
        logger.error("Set port error  ${e.message}")
        status = "Set port ERROR ${pijob.desdevice?.name} ${pijob.name}  ${e.message}"
        mtp.lgs.createERROR("Set port ERROR ${pijob.desdevice?.name} ${pijob.name}  ${e.message}", Date())
        if (token != null)
            ntfs.message("Set port ERROR ${pijob.desdevice?.name} ${pijob.name} ", token)
        else
            ntfs.message("Set port ERROR ${pijob.desdevice?.name} ${pijob.name}  ")

        TimeUnit.SECONDS.sleep(5)


        if (token != null)
            ntfs.message("End job  ${pijob.name}  Can not connect to target ", token)
        else
            ntfs.message("End job  ${pijob.name}   Can not connect to target ")

        status = "End job  ${pijob.name}  Can not connect to target "

        TimeUnit.SECONDS.sleep(5)
//        haveerror = true
//        errormessage = "End job  ${pijob.name}  Can not connect to target"
        //                        continue// setport ต่อ ไป

        //ไม่ทำอะไรข้ามไปเลยแล้วก็จบ job เลยถ้ามีปัญหารอรอบหน้าเลย
    }

//    fun findUrl(portname: String, runtime: Long, waittime: Long, value: Int): String {
//        if (pijob.desdevice != null) {
//            var ip = dhts.mactoip(pijob.desdevice!!.mac!!)
//            if (ip != null) {
//                var url = "http://${ip.ip}/run?port=${portname}&delay=${runtime}&value=${value}&wait=${waittime}"
//                return url
//            }
//        }
//        throw Exception("Error Can not find url")
//    }

//    fun findUrl(target: PiDevice, portname: String, runtime: Long, waittime: Long, value: Int): String {
//        if (pijob.desdevice != null) {
//            var ip = dhts.mactoip(target.mac!!)
//            if (ip != null) {
//                var url = "http://${ip.ip}/run?port=${portname}&delay=${runtime}&value=${value}&wait=${waittime}"
//                return url
//            }
//        }
//        throw Exception("Error Can not find url")
//    }
//


    var logger = LoggerFactory.getLogger(D1hjobWorker::class.java)

    override fun toString(): String {

        return "name ${pijob.name}"
    }


}