package me.pixka.kt.run

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class D1TimerWorker(val p: Pijob,
                    val readvalue: ReadUtil, val pijs: PortstatusinjobService, var test: Pijob? = null)
    : DefaultWorker(p, null, readvalue, pijs, logger) {

    //    var httpControl = HttpControl()
    override fun run() {
        try {
            logger.debug("Start run D1Timer ")
            var t = readvalue.readTmpByjob(p)
            startRun = Date()
            status = "T: ${t}"
            logger.debug("T: ${t}")
            isRun = true
            startRun = Date()
            if (t != null)
                if (checkrang(t)) {

                    //go !!
                    status = "T in rang"
                    try {
                        if (checkhigh()) {

                            //ใช้ข้อมูลของ port run
                            var list = pijs.findByPijobid(p.id)
                            logger.debug("${p.name} Port in job ${list.size}")
                            if (list != null) {
                                status = "Set remote port"
                                setRemoteport(list as List<Portstatusinjob>)
                                if (pijob.waittime != null) {
                                    TimeUnit.SECONDS.sleep(pijob.waittime!!.toLong())
                                }
                                logger.debug("End job ")
                                status = "End job ${pijob.name}"
                                isRun=false
                            } else {
                                status = "no have remote port"
                                logger.error("No have report port to run")
                                isRun = false
                            }

                        } else {
                            status = "Wait high time out "
                            logger.error("Wait high time out ")
                            TimeUnit.SECONDS.sleep(5)
                        }
                    } catch (e: Exception) {
                        logger.error(e.message)
                        status = e.message
                        isRun = false
                    }
                } else {
                    status = "Out of rang"
                    logger.error("Out of rang")
                }
            isRun = false
        } catch (e: Exception) {
            logger.error("D1Timer error")
            status = "${e.message}"
        }

        isRun = false
    }


    var df = SimpleDateFormat("hh:mm:ss")
    override fun setRemoteport(ports: List<Portstatusinjob>) {
        var ee = Executors.newSingleThreadExecutor()
        logger.debug("Set remoteport ${ports}")
        for (port in ports) {

            try {
                var traget = port.device
                var runtime = port.runtime
                var waittime = port.waittime
                var portname = port.portname?.name
                var value = logtoint(port.status!!)


                var ip = readUtil?.findIp(traget!!)
                logger.debug("Found traget ip ${ip}")
                if (ip != null) {

                    try {
                        callNewfunction(port, ip,runtime,waittime)
                    } catch (e: Exception) {
                        logger.error("Error set port ${e.message}")
                        var url = "http://${ip.ip}/run?port=${portname}&value=${value}&delay=${runtime}&waittime=${waittime}"
                        logger.debug("Call to ${url}")

                        var get = HttpGetTask(url)
                        var f = ee.submit(get)
                        try {
                            var value = f.get(3, TimeUnit.SECONDS)
                            logger.debug("Set remote result ${value}")

                        } catch (e: Exception) {
                            logger.error("Can not connect to traget device [${e.message}]")
                        }

                        if (runtime != null) {
                            TimeUnit.SECONDS.sleep(runtime.toLong()) //หยุดรอถ้ามีการกำหนดมา
                        }
                        if (waittime != null) {
                            TimeUnit.SECONDS.sleep(waittime.toLong()) //หยุดรอถ้ามีการกำหนดมา
                        }
                    }

                }
            } catch (e: Exception) {
                logger.error("SetRemote ${e.message}")
            }
        }

    }

    fun callNewfunction(port: Portstatusinjob, ip: Iptableskt, runtime: Int?, waittime:Int?) {
        try {
            var ee = Executors.newSingleThreadExecutor()
            var runtime = port.runtime
            var url2 = "http://${ip.ip}/runcounter?time=${runtime}"
            var http = HttpGetTask(url2)

            var f1 = ee.submit(http)
            try {
                var re = f1.get(60, TimeUnit.SECONDS)
            } catch (e: Exception) {
                logger.error("call url 2 error ${e.message}")
                throw e
            }
            val c = Calendar.getInstance()
            c.add(Calendar.SECOND, runtime!!)
            var nextdate = df.format(c.time)
            var url3 = "http://${ip.ip}/settext?closetime=${nextdate}"

            var get = HttpGetTask(url3)
            var f2 = ee.submit(get)
            try {
                var re = f2.get(60, TimeUnit.SECONDS)
            } catch (e: Exception) {
                logger.error("Call url 3 error ${e.message}")
                throw e
            }

            if (runtime != null) {
                status="Run time ${runtime}"
                logger.debug("Run time ${runtime}")
                TimeUnit.SECONDS.sleep(runtime.toLong()) //หยุดรอถ้ามีการกำหนดมา
            }
            if (waittime != null) {
                status="Wait time  ${waittime}"
                logger.debug("Wait time  ${waittime}")
                TimeUnit.SECONDS.sleep(waittime.toLong()) //หยุดรอถ้ามีการกำหนดมา
            }
            logger.debug("End set new port ")
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }

    }

    fun checkhigh(): Boolean {
        try {
            var timeout = 120 //สองนาที
            if (pijob.hlow != null) {
                timeout = pijob.hlow!!.toInt()
            }
            logger.debug("Check High ${Date()}")
            val high = pijob.thigh?.toFloat()
            while (true) {
                var v = BigDecimal.ZERO
                try {
                    v = readvalue.readTmpByjob(p)
                } catch (e: Exception) {
                    logger.error("Read Tmp error ${e.message}")
                }
                logger.debug("Wait for high ${v} >= ${high}")
                if (v != null) {
                    val tt = v.toFloat()
                    logger.debug("Check HIGH ${tt} > = ${high}")
                    if (tt >= high!!) {
                        logger.debug("This job can run now")
                        return true
                    } else {
                        logger.debug("This job have to wait")
                    }
                }
                TimeUnit.SECONDS.sleep(1) // รอ 1 วินาที
                timeout--
                logger.debug("Check high timeout ${timeout}")
                if (timeout <= 0) {
                    status = "Check high time out"
                    logger.error("Check high time out")
                    return false
                }
            }
        } catch (e: Exception) {
            logger.error("Check high  ${e.message}")
            status = "Check high ${e.message}"
            return false
        }

    }

    fun checkrang(t: BigDecimal): Boolean {
        logger.debug("Check rang")
        val l = p.tlow?.toFloat()
        val h = p.thigh?.toFloat()
        val v = t.toFloat()
        logger.debug("${l} =< ${t}  <= ${h}")
        status = "${l} =< ${t}  <= ${h}"
        if (l != null && h != null) {
            if (l <= v && v <= h) {
                status = "In rang"
                logger.debug("In rang")
                return true
            }
        }
        status = "out of rang"
        logger.error("Check rang Out of rang")
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(D1TimerWorker::class.java)
    }

    override fun toString(): String {
        return "D1Timer ${pijob.name}"
    }
}
