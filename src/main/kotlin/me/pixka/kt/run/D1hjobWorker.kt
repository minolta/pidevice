package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Logistate
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.d.ConfigdataService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.WarterLowPressureService
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class D1hjobWorker(
    var job: Pijob,
    val mtp: MactoipService, val ntfs: NotifyService, val lps: WarterLowPressureService,val cfg:ConfigdataService
) : DWK(job), Runnable {
    var om = ObjectMapper()
    var waitstatus = false
    var token: String? = null

    var longtime = false //บอกว่าถ้าระบบต้องรอแรงดันต้อง set time open ใหม่
    /**
     * สำหรับ ดูว่าระบบน้ำ ok หรือเปล่า
     */
    fun canrun(): Boolean {
        return lps.canuse
    }

    fun notify(msg: String) {
        try {
            if (token != null) {
                ntfs.message(msg, token!!)
            }
        } catch (e: Exception) {
            status = "Error in notify status ${e.message} ${msg}"
            logger.error("ERROR in notify ${e.message} ")
            throw e
        }
    }

    var psi: Double? = null

    /**
     * ตรวจแรงดันเองว่า ok ตาม tlow กำหนด
     */
    fun checkPressure(p: Pijob): Boolean {

        if (pijob.tlow == null || pijob.tlow!!.toDouble() == 0.0)
            return true //ไม่มีการ check pressure


        try {
            var pij = mtp.pus.bypijobid(p.id)
            if (pij == null)
                return false
            var devicename = ""
            var psivalue = ""
            pij.forEach {
                try {
                    devicename = it.pidevice?.name!!
                    psi = mtp.readPressure(it.pidevice!!)
                    psivalue = "PSI:${psi}"
                    if (psi == null) {
                        logger.error("WORKER D1h error ${pijob.name} ERROR:  Can not read pressure")
                        status = "WORKER D1h error ${pijob.name} ERROR:  Can not read pressure"
                    }
                    var setp = p.tlow?.toDouble()
                    //ถ้าอ่านและสูงกว่าก็ให้ job ทำงาน
                    if (setp!! <= psi!!)
                        return true
                } catch (e: Exception) {
                    status = "Error ${e.message} Check pressure devicename: ${devicename}  ${psivalue}"
                    TimeUnit.SECONDS.sleep(1)
                }
            }
            logger.error("${pijob.name} Pressure is low  Read psi ${psi}  set PSI ${pijob.tlow}")
            if (token != null)
                ntfs.message("${pijob.name} Pressure is low  Read psi ${psi}  set PSI ${pijob.tlow}", token!!)
            status = "${pijob.name} Pressure is low  Read psi ${psi}  set PSI ${pijob.tlow}"
            TimeUnit.SECONDS.sleep(1)
            //ถ้าอ่าน psi ของแต่ละปั๊มไม่ได้ก็ไม่ผ่าน
            return false
        } catch (e: Exception) {
            logger.error("WORKER D1h error ${pijob.name} ERROR: " + e.message)
            status = "WORKER D1h error ${pijob.name} ERROR: ${e.message}"
            TimeUnit.SECONDS.sleep(1)
        }


        status = "Pressure not  ok"
        return false
    }

    fun openPumpinpijob(pj: Pijob, timetoopen: Int) {
        try {
            var pumpsinpijob = mtp.pus.bypijobid(pj.id)
            if (pumpsinpijob != null) {
                pumpsinpijob.forEach {
                    try {
                        status = "Open pump ${it.pidevice?.name} Time:${timetoopen}"
                        TimeUnit.SECONDS.sleep(1)
                        mtp.openpumps(it.pidevice!!, timetoopen)
                    } catch (e: Exception) {
                        status = "${pijob.name} : Error Open pump in pijob PUMPNAME:${it.pidevice?.name} s ${e.message}"
                        logger.error("${pijob.name} : Error Open pump in pijob PUMPNAME:${it.pidevice?.name} s ${e.message}")
                        TimeUnit.SECONDS.sleep(2)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR openPumpinpijob ${e.message}")
            status = "ERROR openPumpinpijob ${e.message}"
            TimeUnit.SECONDS.sleep(1)
        }

    }

    fun openpump(time:Int=0) {
        try {
            var openpumptime =  time

            if(time ==0 )
                openpumptime = mtp.findTimeofjob(pijob)
            if (pijob.thigh != null) {
                openpumptime = openpumptime + pijob.thigh!!.toInt()
            }
            openpumptime = openpumptime + cfg.getValue("openpumptime","2").valuename!!.toInt() //สำหรับเวลาเกิดปัญหาหรือเปิดช้า ไปนิดหนึ่ง
            status = "Time of job : ${openpumptime}"
            openPumpinpijob(pijob, openpumptime)
            notify("JOB ${pijob.name} Open Pump Delay 10")

            status = "Open pump ok"
            TimeUnit.SECONDS.sleep(1)
        } catch (e: Exception) {
            logger.error("Open pumps error ${e.message}")
            status = "Open pumps error ${e.message}"
            TimeUnit.SECONDS.sleep(1)
        }
    }


    fun checkperssureLoop(): Boolean {

        var waitloop = 30

        if (pijob.thigh != null)
            waitloop = pijob.thigh!!.toInt()

        for (i in 1..waitloop) {
            if (!checkPressure(pijob)) {
                status = "wait pressure ${psi}  loop time ${i}/${waitloop}"
                TimeUnit.SECONDS.sleep(1)
                longtime = true //บอก thread ว่า set ระบบแบบใหม่ openport แบบ ใหม่
            } else {
                return true
            }
        }

        return false
    }

    fun reportlowpressure() {
        lps.reportLowPerssure(pijob.desdevice!!.id, 0.0, pijob.desdevice, pijob)
        if (!lps.canuse) {
            for (i in 0..40) {
                //ถ้าไม่สามารถ run แล้วให้  notify เลย
                if (pijob.token != null) {
                    ntfs.message("Low Pressure H job stop !!! Check pipe or Warter System", pijob.token!!)
                } else
                    ntfs.message("Low Pressure H job stop !!! Check pipe or Warter System")
                TimeUnit.SECONDS.sleep(1)
            }
        }
    }

    fun resetlowpressure() {
        //ถ้า pressure ok แล้วก็ clear ให้หมด
        lps.reset()
    }

    override fun run() {
        startRun = Date()
        token = pijob.token
        if (!canrun()) {
            status = "Can not run Service not allow"
            notify("Can not run Service not allow JOB:${pijob.name}")
            return
        }
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        openportdownpressure()
        openpump()
        try { //ถ้ามีการำกำหนด Tlow ระบบ จะทำการตรวจสอบแรงดันตามกำหนด
            if (!checkperssureLoop()) {
                waitstatus = true //บอกว่าไม่ใช้น้ำแล้วแต่ยังรออยู่ บอกให้ job อื่นทำงานต่อ
                if (pijob.thigh != null) {
                    status = "Wait by T high ... ${pijob.thigh!!.toLong()}"
                    TimeUnit.SECONDS.sleep(pijob.thigh!!.toLong())
                } else {
                    status = "Wait ... 600"
                    TimeUnit.SECONDS.sleep(600) //หยุดรอไป 10 นาที
                }
                isRun = false
                logger.error(" ${pijob.name} Pressure is low")
                status = "Exit Pressure is low "
                reportlowpressure()
                return
            } else {
                resetlowpressure()
            }
        } catch (e: Exception) {
            logger.error("ERROR PiJOB:  ${pijob.name} ${e.message}")
        }


        try {
            Thread.currentThread().name = "JOBID:${pijob.id} D1H : ${pijob.name} "
            status = "Set port now"
            waitstatus = false //ใช้น้ำ
            goII()
            waitstatus = true //หยุดใช้น้ำแล้ว
            exitdate = findExitdate(pijob)
            if (exitdate == null)
                isRun = false
            status = "End normal"
            notify("End job ${pijob.name}")
            return
        } catch (e: Exception) {
            isRun = false
            exitdate = findExitdate(pijob)
            logger.error("ERROR 1 ${e.message}")
            status = "ERROR 1 ${e.message}"
            notify("ERROR 1 ${e.message} JOB:${pijob.name}")
            mtp.lgs.createERROR(
                "ERROR 1 ${e.message}", Date(), "D1hjobWorker",
                "", "", "", "", pijob.refid
            )
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

    /**
     * สำหรับบอก port ว่าทำยังไง
     */
    fun setport(it: Portstatusinjob) {

        var pw = it.waittime
        var pr = it.runtime
        var pn = it.portname!!.name
        var v = it.status

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
            //30 วิถ้าติดต่อไม่ได้ให้หยุดเลย
            if(longtime)
            {
                //ถ้า longtime จะเปิดเป็น port ต่อ port
                var newtimeopenpump = 0
                if(pw!=null)
                    newtimeopenpump = pw.toInt()
                if(pr!=null)
                    newtimeopenpump+= pr.toInt()
                openpump(newtimeopenpump)
            }
            var v = mtp.setport(it, 15000)
            status = "Delay  ${runtime} + ${waittime}"
            var s = om.readValue<Status>(v)
            logger.debug("D1h Value ${status}")
            status =
                "Set Device ${it.device?.name} port ${portname} to ${value}  ${s.status} and run ${runtime} sec at ${psi} psi"
            notify("JOB ${pijob.name} Set ${portname} to ${value}  ${s.status} and run ${runtime}")
            TimeUnit.SECONDS.sleep(runtime)
            if (waittime != null) {
                status = "Wait time of port ${waittime}"
                TimeUnit.SECONDS.sleep(waittime)
            }
        } catch (e: ConnectException) {
            haveError(token, e, it)
        } catch (e: TimeoutException) {
            haveError(token, e, it)
        }
    }

    /**
     * สำหรับเปิดแรงดันออกก่อนให้ปั๊มทำงานบ้างครั้งยังไม่ถึงแรงดันเปิดและแรงดันปิดทำให้ระบบรอ
     */
    fun openportdownpressure() {
        try {
            var ports = mtp.getPortstatus(pijob, true)
            if (ports != null && ports.size > 0) {
               var port =  ports.get(0)
                if(port!=null)
                {
                    mtp.setport(port, 2)
                }
            }
        } catch (e: Exception) {
            logger.error("Open pressure ${e.message}")
        }
    }

    //เพิ่มการหยุดไปด้วย
    fun goII() {
        var ports = mtp.getPortstatus(pijob, true)
        var wt = 0 //เวลาที่รอแรงดัน
        var token = pijob.token
        ports?.forEach {
            if (!isRun) {
                status = "Exit by user"
                return
            }
            status = "Run set  Device: ${it.device?.name} port ${it.portname?.name} state ${it.status?.name}"
            try {
                startRun = Date()

                if (checkperssureLoop()) {
                    setport(it)
                } else {
                    status =
                        "Perssure not ok exit set port ${it.device?.name} port ${it.portname?.name} state ${it.status?.name}"
                    TimeUnit.SECONDS.sleep(1)
                }


            } catch (e: Exception) {
                logger.error("Error 2 ${e.message}")
                status = " Error 2 ${e.message}"
                notify("JOB: ${pijob.name} Error 2 ${e.message}")
                mtp.lgs.createERROR(
                    " Error 2 ${e.message}", Date(), "D1hjobWorker",
                    "", "", "goII()", pijob.desdevice?.mac, pijob.refid
                )
                isRun = false
                waitstatus = true //หยุดใช้น้ำแล้ว
            }

        }


    }


    fun haveError(token: String?, e: Exception, it: Portstatusinjob) {
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

        status = "End job  ${pijob.name}  Can not connect to target  ${it.device?.name}  port ${it.portname?.name}"

        if (pijob.thigh != null) {
            TimeUnit.SECONDS.sleep(pijob.thigh!!.toLong()) //ถ้ามีการ set thigh มากให้ รอตามนั้นเลย
        } else
            TimeUnit.SECONDS.sleep(600)
        isRun = false //ออกเลย

        //ไม่ทำอะไรข้ามไปเลยแล้วก็จบ job เลยถ้ามีปัญหารอรอบหน้าเลย


    }


    var logger = LoggerFactory.getLogger(D1hjobWorker::class.java)

    override fun toString(): String {

        return "name ${pijob.name}"
    }


}