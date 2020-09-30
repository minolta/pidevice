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
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*

class D1portjobWorker(var pijob: Pijob, val service: PijobService,
                      val httpService: HttpService,
                      val task: TaskService, val ips: IptableServicekt, val lgs: LogService)
    : PijobrunInterface, Runnable {
    var isRun = false
    var state = "Init"
    var startrun: Date? = null
    var waitstatus = false
    val mapper = ObjectMapper()
    var exitdate: Date? = null
    val om = ObjectMapper()

    override fun setG(gpios: GpioService) {
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        return this.pijob.id
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return startrun
    }

    override fun state(): String? {
        return state
    }

    fun findExitdate(pijob: Pijob): Date? {
        var t = 0L
        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!

        t += (rt + wt) //เวลานานสุดของ runport
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            return null

        return exitdate
    }

    override fun run() {

        startrun = Date()
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        Thread.currentThread().name = "JOBID:${pijob.id} D1PORT : ${pijob.name} ${startrun}"
        try {
            waitstatus = false
            goII()
            waitstatus = true
//            exitdate = findExitdate(pijob)
//            state = "End job and wait"
        } catch (e: Exception) {
            this.state = "Run By port is ERROR ${e.message}"
            logger.error("Run By port is ERROR ${e.message}")
            lgs.createERROR("${e.message}", Date(),
                    "D1readportWorker", "", "",
                    "run", pijob.desdevice?.mac)
            waitstatus = true
            isRun = false
        }

        waitstatus = true
        findExitdate(pijob)
//        isRun = false
//        state = "End job"
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
                    var url = findUrl(it.device!!, pn!!, pr.toLong(), pw.toLong(), value)
                    startrun = Date()
                    logger.debug("URL ${url}")
                    state = "Set port ${url}"
                    var setportreturn = httpService.get(url,500)
                    var status = om.readValue<Status>(setportreturn)
                    state = "Set port:${pn}  to ${pr}  value : ${value} ok "
                } catch (e: Exception) {
                    lgs.createERROR("${e.message}", Date(), "D1ReadportWorker",
                            "", "", "", it.device?.mac,it.pijob?.refid
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


    fun findUrl(target: PiDevice, portname: String, runtime: Long, waittime: Long, value: Int): String {
        if (pijob.desdevice != null) {
            var ip = ips.findByMac(target.mac!!)
            if (ip != null) {
                var url = "http://${ip.ip}/run?port=${portname}&delay=${runtime}&value=${value}&wait=${waittime}"
                return url
            }
        }

        throw Exception("Error Can not find url")
    }


    override fun setP(pijob: Pijob) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                  var value: Int? = null,var mac:String?=null) {
    override fun toString(): String {
        return "D1:${d1} D2:${d2} D3:${d3} D4:${d4} D5:${d5} D6:${d6} D7:${d7} D8:${d8}"
    }
}
