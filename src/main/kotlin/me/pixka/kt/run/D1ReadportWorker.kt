package me.pixka.kt.run


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.d.ErrorlogII
import me.pixka.kt.pidevice.d.ErrorlogServiceII
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class D1portjobWorker(var pijob: Pijob, val service: PijobService,
                      val dhts: Dhtutil, val httpControl: HttpControl, val task: TaskService)
    : PijobrunInterface, Runnable {
    var isRun = false
    var state = "Init"
    var startrun: Date? = null
    var waitstatus = false
    val mapper = ObjectMapper()
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

    fun getSensorstatus(p: Pijob): DPortstatus? {
        try {
            var ip = dhts.mactoip(p.desdevice?.mac!!)
            var url = "http://${ip?.ip}"
            var ee = Executors.newSingleThreadExecutor()
            var get = HttpGetTask(url)
            var f2 = ee.submit(get)
//            val re = httpControl.get(url)
            var re: String? = null

            try {
                re = f2.get(5, TimeUnit.SECONDS)
            } catch (e: Exception) {
                state = "Time out to get level water status"
                TimeUnit.SECONDS.sleep(10)
                throw Exception("Time out to get level water status")
            }
                var dp = mapper.readValue(re, DPortstatus::class.java)
                return dp



        } catch (e: Exception) {
            logger.error("Get Sensor status ${e.message}")
        }
        return null
    }

    fun getsensorstatusvalue(n: String, c: Int, sensorstatus: DPortstatus?): Boolean {
        val nn = n.toLowerCase()
        try {
            if (nn.equals("d1")) {
                if (sensorstatus?.d1 == c)
                    return true
            } else if (nn.equals("d2")) {
                if (sensorstatus?.d2 == c)
                    return true
            } else if (nn.equals("d3")) {
                if (sensorstatus?.d3 == c)
                    return true
            } else if (nn.equals("d4")) {
                if (sensorstatus?.d4 == c)
                    return true
            } else if (nn.equals("d5")) {
                if (sensorstatus?.d5 == c)
                    return true
            } else if (nn.equals("d6")) {
                if (sensorstatus?.d6 == c)
                    return true
            } else if (nn.equals("d7")) {
                if (sensorstatus?.d7 == c)
                    return true
            } else if (nn.equals("d8")) {
                if (sensorstatus?.d8 == c)
                    return true
            }
        } catch (e: Exception) {
            logger.error("ERROR in getsensorstatusvalue message: ${e.message}")
        }


        return false
    }


    fun checkCanrun(): Boolean {

        //เอา port ที่สำหรับไว้ตรวจสอบออกมา
        var checks = getPorttocheck(pijob)
        var sensorstatus = getSensorstatus(pijob)
        logger.debug("Checks ${checks} Status:${sensorstatus}")

        if (checks != null) {
            var r = false
            for (c in checks) {
                r = r || getsensorstatusvalue(c.name!!, c.check!!, sensorstatus!!)
            }
            logger.debug("R is ${r}")
            return r
        }
        return false
    }

    fun getPorttocheck(p: Pijob): ArrayList<PorttoCheck>? {
        try {
            var bufs = ArrayList<PorttoCheck>()
            logger.debug("Description ${p.description}")
            var c = p.description?.split(",")
            logger.debug("C: ${c}")
            if (c.isNullOrEmpty()) {
                return null
            }

            var c1 = PorttoCheck()
            var ii = 1

            c.map {
                logger.debug("Value : ${it}")
                if (it.toIntOrNull() == null)
                    c1.name = it
                else {
                    c1.check = it.toInt()
                    bufs.add(c1)
                    c1 = PorttoCheck()
                }
            }

            return bufs


        } catch (e: Exception) {
            logger.debug("ERROR ${e.message}")
        }
        return null
    }

    override fun run() {

        startrun = Date()
        isRun = true
        waitstatus = false //เริ่มมาก็ทำงาน
        Thread.currentThread().name = "JOBID:${pijob.id} D1PORT : ${pijob.name} ${startrun}"
        try {
            if (checkCanrun()) {
                waitstatus = false
                go()
            }
            waitstatus = true
            isRun = false
            state = "End job"
        } catch (e: Exception) {
            logger.error("Run By port is ERROR ${e.message}")
        }

        waitstatus = true
        isRun = false
        state = "End job"
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
                        TimeUnit.SECONDS.sleep(10)
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
                    TimeUnit.SECONDS.sleep(10)
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
            } else if (run is D1portjobWorker) {
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
                  var d7: Int? = 0, var d8: Int? = 0, var name: String? = null, var value: Int? = null) {
    override fun toString(): String {
        return "D1:${d1} D2:${d2} D3:${d3} D4:${d4} D5:${d5} D6:${d6} D7:${d7} D8:${d8}"
    }
}
