package me.pixka.kt.run


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*

class D1portjobWorker(job: Pijob, var mtp: MactoipService) : DWK(job), Runnable {


    var waitstatus: Boolean = false
    override fun run() {

        startRun = Date()
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        Thread.currentThread().name = "JOBID:${pijob.id} D1PORT : ${pijob.name} ${startRun}"
        try {
            waitstatus = false
            goII()
            waitstatus = true
        } catch (e: Exception) {
            this.status = "Run By port is ERROR ${e.message}"
            logger.error("Run By port is ERROR ${e.message}")
            mtp.lgs.createERROR("${e.message}", Date(),
                    "D1readportWorker", "", "",
                    "run", pijob.desdevice?.mac, pijob.refid, pijob.pidevice_id)
            waitstatus = true
            isRun = false
        }

        waitstatus = true
        exitdate = findExitdate(pijob)
    }

    var rt = 0
    var wt = 0
    fun goII() {
        var ports = pijob.ports
        if (ports != null) {
            ports.filter {
                it.enable != null && it.enable == true
            }.forEach {
                var pw = it.waittime
                var pr = it.runtime
                //หาเวลาที่มากสุดไปรวมกับเวลาของ main job เพื่อให้ออกไปหยุดรอ
                if (pw!! > wt) {
                    wt = pw
                }
                if (pr!! > rt) {
                    rt = pr
                }
                var pn = it.portname!!.name
                var v = it.status //สำหรับบอก port ว่าจะเป็น logic อะไร
                var value = getLogic(v?.name!!)
                try {
                    var re = mtp.setport(it)
                    var st = mtp.om.readValue<Status>(re)
                } catch (e: Exception) {
                    mtp.lgs.createERROR("${e.message}", Date(),
                            "D1ReadportWorker",
                            "", "", "", it.device?.mac,
                            it.pijob?.refid
                    )
                }
            }
        }
    }

    fun getLogic(v: String): Int {
        var value = 0
        if (v.equals("high") || v.indexOf("1") != -1) {
            value = 1
        } else
            value = 0
        return value
    }

    override fun toString(): String {
        return "name ${pijob.name}"
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(D1portjobWorker::class.java)
    }
}

//สำหรับเก็บว่า pijob นั้นกำหนดให้ port ค่าอะไรบ้าง
class PorttoCheck(var name: String? = null, var check: Int? = null) {
    override fun toString(): String {
        return "name:${name} Check ${check}"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class DPortstatus(var version: Int? = null,
                  var d1: Int? = null, var d2: Int? = 0, var d3: Int? = 0,
                  var d4: Int? = 0, var d5: Int? = 0, var d6: Int? = 0,
                  var d7: Int? = 0, var d8: Int? = 0, var name: String? = null,
                  var value: Int? = null, var mac: String? = null) {
    override fun toString(): String {
        return "D1:${d1} D2:${d2} D3:${d3} D4:${d4} D5:${d5} D6:${d6} D7:${d7} D8:${d8}"
    }
}
