package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Logistate
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DhtvalueService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class D1hjobWorker(var pijob: Pijob,
                   val dhts: Dhtutil, val httpService: HttpService,
                   val task: TaskService, val ntfs: NotifyService)
    : PijobrunInterface, Runnable {
    var isRun = false
    var state = "Init"
    var startrun: Date? = null
    var waitstatus = false
    var haveerror = false
    var errormessage = ""
    var exitdate: Date? = null
    var om=ObjectMapper()

    override fun setG(gpios: GpioService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        if (pijob != null) {
            return this.pijob.id
        }
        throw Exception("Pijob is null")
    }

    override fun getPJ(): Pijob {
        if (pijob != null) {
            return pijob
        }
        throw Exception("Pi job is null from getPJ")
    }

    override fun startRun(): Date? {
        return startrun
    }

    override fun state(): String? {
        return state
    }



    /*
    * เป็นการ จบ Thread แต่ยังไม่เอาออกจาก list เพราะต้องคืน Core ให้กับระบบ แต่ run อยู่
    * */
    fun setEnddate() {
        var t = 0L

        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.

        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            isRun = false//ออกเลย
    }

    override fun run() {
        startrun = Date()
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        Thread.currentThread().name = "JOBID:${pijob.id} D1H : ${pijob.name} ${startrun}"
        try {
            if (pijob.tlow != null) {//delay ก่อน
                TimeUnit.SECONDS.sleep(pijob.tlow!!.toLong())
                logger.debug("Slow start ${pijob.tlow}")
            }
            waitstatus = false //ใช้น้ำ
            go()
            waitstatus = true //หยุดใช้น้ำแล้ว
            exitdate = task.findExitdate(pijob)
            if (exitdate == null)
                isRun = false
            state = "End normal"
            return
        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR 1 ${e.message}")
            state = "ERROR 1 ${e.message}"
            waitstatus = true
            throw e
        }

//        waitstatus = true
//        isRun = false
//        state = "End job"
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
                state = "Value to set ERROR ${v.name}"
            else
                state = "Port state Error is Null"

        }

        return value
    }

    fun go() {//Run
//        var ee = Executors.newSingleThreadExecutor()
        var token = pijob.token
        state = "Run set port "
        var ports = pijob.ports
        logger.debug("Ports ${ports}")
        if (ports != null)
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
                    var url = ""

                    try {
                        if (port.device != null)
                            url = findUrl(port.device!!, portname!!, runtime, waittime, value)
                        else
                            url = findUrl(portname!!, runtime, waittime, value)
                    } catch (e: Exception) {
                        logger.error("Find URL ERROR ${e.message} port: ${port} portname ${portname}")
                        state = "Find URL ERROR ${e.message} port: ${port} portname ${portname}"
                    }
                    startrun = Date()
                    logger.debug("URL ${url}")
                    state = "Set port ${url}"
                    try {
                        //30 วิถ้าติดต่อไม่ได้ให้หยุดเลย
                        var v = httpService.get(url)
                        state = "Delay  ${runtime} + ${waittime}"
                        var status = om.readValue<Status>(v)
                        logger.debug("D1h Value ${status}")
                        state = "Set ${portname} to ${value}  ${status.status} and run ${runtime}"
                        TimeUnit.SECONDS.sleep(runtime)

                        if (waittime != null) {
                            state = "Wait time of port ${waittime}"
                            TimeUnit.SECONDS.sleep(waittime)
                        }
                    } catch (e: ConnectException) {
                        haveError(token, e)
                    } catch (e: TimeoutException) {
                        haveError(token, e)
                    }
                } catch (e: Exception) {
                    logger.error("Error 2 ${e.message}")
                    state = " Error 2 ${e.message}"
                    isRun = false
                    waitstatus = true //หยุดใช้น้ำแล้ว
                    break //ออกเลย

                }
            } //end for

        if (!haveerror)
            state = "Set port ok "
        else
            state = "Set port error "
    }

    fun haveError(token: String?, e: Exception) {
        logger.error("Set port error  ${e.message}")
        state = "Set port ERROR ${pijob.desdevice?.name} ${pijob.name}  ${e.message}"
        if (token != null)
            ntfs.message("Set port ERROR ${pijob.desdevice?.name} ${pijob.name} ", token)
        else
            ntfs.message("Set port ERROR ${pijob.desdevice?.name} ${pijob.name}  ")

        TimeUnit.SECONDS.sleep(5)


        if (token != null)
            ntfs.message("End job  ${pijob.name}  Can not connect to target ", token)
        else
            ntfs.message("End job  ${pijob.name}   Can not connect to target ")

        state = "End job  ${pijob.name}  Can not connect to target "

        TimeUnit.SECONDS.sleep(5)
        haveerror = true
        errormessage = "End job  ${pijob.name}  Can not connect to target"
        //                        continue// setport ต่อ ไป

        //ไม่ทำอะไรข้ามไปเลยแล้วก็จบ job เลยถ้ามีปัญหารอรอบหน้าเลย
    }

    fun findUrl(portname: String, runtime: Long, waittime: Long, value: Int): String {
        if (pijob.desdevice != null) {
            var ip = dhts.mactoip(pijob.desdevice!!.mac!!)
            if (ip != null) {
                var url = "http://${ip.ip}/run?port=${portname}&delay=${runtime}&value=${value}&wait=${waittime}"
                return url
            }
        }

        throw Exception("Error Can not find url")
    }

    fun findUrl(target: PiDevice, portname: String, runtime: Long, waittime: Long, value: Int): String {
        if (pijob.desdevice != null) {
            var ip = dhts.mactoip(target.mac!!)
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

    companion object {
        internal var logger = LoggerFactory.getLogger(D1hjobWorker::class.java)
    }

    override fun toString(): String {

        return "name ${pijob.name}"
    }

    fun checkgroup(job: Pijob): Pijob? {
        var runs = task.runinglist

        for (run in runs) {
            if (run is D1hjobWorker) {
                //ถ้า job รอแล้ว
                logger.debug("Wait status is ${run.waitstatus} RunGROUPID ${run.pijob.pijobgroup_id} " +
                        "JOBGROUPID ${job.pijobgroup_id} ")

                if (/*ทำงานอยู่*/run.isRun && /*อยู่ในการพักอยู่*/ !run.waitstatus &&
                        /*ไม่ใช่ตัวเอง*/run.getPijobid().toInt() != job.id.toInt()) {
                    if (run.pijob.pijobgroup_id?.toInt() == job.pijobgroup_id?.toInt()) {
                        return null //อยู่ในกลุ่มเดียวกัน
                    }
                }
            }
        }
        logger.debug("No Job in this group run ${job}")
        return job
    }
}