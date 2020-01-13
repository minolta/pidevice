package me.pixka.kt.run

import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.d.ErrorlogII
import me.pixka.kt.pidevice.d.ErrorlogServiceII
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class D1hjobWorker(var pijob: Pijob, val dhtvalueService: DhtvalueService,
                   val dhts: Dhtutil, val httpControl: HttpControl, val task: TaskService)
    : PijobrunInterface, Runnable {
    var isRun = false
    var state = "Init"
    var startrun: Date? = null
    var waitstatus = false

    @Autowired
    lateinit var errorlog: ErrorlogServiceII

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

    fun checkCanrun(): Boolean {
        var h: Float? = 0.0F
        try {
            var dhtvalue = dhts.readByPijob(pijob)
            logger.debug("DHTVALUE ${dhtvalue}")
            state = "Get value from traget [${dhtvalue}]"
            if (dhtvalue != null) {
                if (dhtvalue.h != null)
                    h = dhtvalue.h?.toFloat()
                if (checkH(pijob.hlow?.toFloat()!!, pijob.hhigh?.toFloat()!!, dhtvalue.h?.toFloat()!!)) {
                    return true
                } else {
                    state = "H not in ranger HLOW ${pijob.hlow} HHIGH ${pijob.hhigh} H:${dhtvalue.h}"
                    logger.error("H not in ranger HLOW ${pijob.hlow} HHIGH ${pijob.hhigh} H:${dhtvalue.h}")
                }

            }
        } catch (e: Exception) {
            logger.error("Check Can run ERROR ${e.message}")
            state = "Check Can run ERROR ${e.message} H not in ranger HLOW ${pijob.hlow} HHIGH ${pijob.hhigh} H:${h}"


        }
        return false
    }

    override fun run() {

        startrun = Date()
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        Thread.currentThread().name = "JOBID:${pijob.id} D1H : ${pijob.name} ${startrun}"

        try {
            if (pijob.tlow != null) {

                TimeUnit.SECONDS.sleep(pijob.tlow!!.toLong())
                logger.debug("Slow start ${pijob.tlow}")
                //prility
            }

            if (checkCanrun()) {
                waitstatus = false
                go()
                waitstatus = true
                var waittime = pijob.waittime
                if (waittime != null) {
                    state = "Wait time of job ${waittime}"
                    TimeUnit.SECONDS.sleep(waittime)
                }
            } else {
                logger.warn("Dht not found")
                waitstatus = true
                isRun = false
                state = "End job"
            }


        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR 1 ${e.message}")
            state = "ERROR 1 ${e.message}"
            waitstatus = true
            throw e
        }

        waitstatus = true
        isRun = false
        state = "End job"
    }

    fun checkH(l: Float, h: Float, v: Float): Boolean {
        state = "Check value ${l} < ${v} > ${h}"
        if (v >= l && v <= h) {
            logger.debug("Run this job ${pijob.name}")
            return true
        }
        logger.debug("Check value ${l} < ${v} > ${h} Not run this job ${pijob.name}")
        TimeUnit.SECONDS.sleep(2)
        return false
    }

    fun go() {//Run
        var ee = Executors.newSingleThreadExecutor()
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
                var value = 0
                if (v != null && v.name != null) {
                    if (v.name.equals("high") || v.name?.indexOf("1") != -1) {
                        value = 1
                    } else value = 0
                } else {
                    if (v != null)
                        state = "Value to set ERROR ${v.name}"
                    else
                        state = "Port state Error is Null"

                    TimeUnit.SECONDS.sleep(5)
                }

                try {
                    var url = ""

                    try {
                        if (port.device != null)
                            url = findUrl(port.device!!, portname!!, runtime, waittime, value)
                        else
                            url = findUrl(portname!!, runtime, waittime, value)
                    } catch (e: Exception) {
                        logger.error("Find URL ERROR ${e.message} port: ${port} portname ${portname}")
                    }
//                    logger.debug("URL ${url}")
                    startrun = Date()
                    logger.debug("URL ${url}")
                    state = "Set port ${url}"
                    var get = HttpGetTask(url)
                    var f = ee.submit(get)
                    try {
                        var value = f.get(30, TimeUnit.SECONDS)
                        state = "Delay  ${runtime} + ${waittime}"
                        logger.debug("D1h Value ${value}")
                        state = "${value} and run ${runtime}"
                        TimeUnit.SECONDS.sleep(runtime)

                        if (waittime != null) {
                            state = "Wait time of port ${waittime}"
                            TimeUnit.SECONDS.sleep(waittime)
                        }
                    } catch (e: Exception) {
                        logger.error("Set port error  ${e.message}")
                        errorlog.save(ErrorlogII("Set port error ${portname} ${pijob.name} ", Date(), port.device!!))
                    }
                } catch (e: Exception) {
                    logger.error("Error 2 ${e.message}")
                    state = " Error 2 ${e.message}"
                    ee.shutdownNow()
                }
            }

        state = "Set port ok "
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