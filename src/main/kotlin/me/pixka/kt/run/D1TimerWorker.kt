package me.pixka.kt.run

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class D1TimerWorker(val p: Pijob, var ips: IptableServicekt,
                    val readvalue: ReadUtil, val pijs: PortstatusinjobService, var test: Pijob? = null,
                    val line: NotifyService)
    : DefaultWorker(p, null, readvalue, pijs, logger) {

    //    var httpControl = HttpControl()
    override fun run() {
        try {
            logger.debug("Start run D1Timer ")
            var t: DS18value? = null
            try {
                t = readvalue.readKtype(pijob)
            } catch (e: Exception) {
                logger.error("Read tmp not found ${e.message}")
                status = "Read tmp not found ${e.message}"
            }
            startRun = Date()
            status = "T: ${t}"
            Thread.currentThread().name = "JOBID:${pijob.id} D1Timer : ${pijob.name} ${startRun}"
            logger.debug("T: ${t}")
            isRun = true
            startRun = Date()
            if (t != null) {
                if (checkrang(t.t!!)) {

                    //go !!
                    status = "T in rang"
                    try {
                        if (checkhigh()) {

                            //ใช้ข้อมูลของ port run
                            if (pijob.token != null) {
                                line.message("Start job ${pijob.name} at ${Date()} ", pijob.token!!)
                            }
                            var list = pijs.findByPijobid(p.id)
                            logger.debug("${p.name} Port in job ${list.size}")
                            if (list != null) {
                                status = "Set remote port"
                                setRemoteport(list as List<Portstatusinjob>)
                                if (pijob.waittime != null) {
                                    status = "wait status job waittime ${pijob.waittime}"
                                    TimeUnit.SECONDS.sleep(pijob.waittime!!.toLong())
                                }
                                logger.debug("End job ")
                                status = "End job ${pijob.name}"
                                isRun = false
                            } else {
                                status = "no have remote port"
                                logger.error("No have report port to run")
                                isRun = false
                                throw Exception("No have report port to run")
                            }

                        } else {
                            status = "Wait high time out "
                            logger.error("Wait high time out ")
                            TimeUnit.SECONDS.sleep(5)
                        }
                    } catch (e: Exception) {
                        logger.error("ERROR Check high ${e.message}")
                        status = "ERROR Check high ${e.message}"
                        isRun = false
                    }
                } else {
                    status = "Out of rang ${t.t}"
                    logger.error("Out of rang")
                    isRun = false
                    throw  Exception("Out of rang")
                }
                isRun = false
            } else {
                logger.error("T is null")
                status = "T is null"
                isRun = false
                throw Exception("T is null")
            }
        } catch (e: Exception) {
            logger.error("D1Timer error ${e.message}")
            status = "Total error ${e.message}"
            isRun = false
            throw e
        }

        isRun = false
    }


    var df = SimpleDateFormat("hh:mm:ss")
    override fun setRemoteport(ports: List<Portstatusinjob>) {

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
//                        try {
//                            callNewfunction(port, ip, runtime, waittime)
//                        } catch (e: Exception) {
//                            logger.error("Call new fun error ${e.message}")
//                            old(ip, port.portname?.name!!, value, runtime, waittime)
//                        }
                        old(ip, port.portname?.name!!, value, runtime, waittime)
                        display(runtime, ip)

                    } catch (e: Exception) {

                        logger.debug("D1Timer set remote port Error ${e.message}")
                        status = "D1Timer set remote port Error ${e.message}"
                    }

                }
            } catch (e: Exception) {
                logger.error("SetRemote ${e.message}")
                status = "SetRemote ${e.message}"
            }
        }

    }

    fun display(runtime: Int?, ip: Iptableskt) {
        val c = Calendar.getInstance()
        c.add(Calendar.SECOND, runtime!!)
        var nextdate = df.format(c.time)
        var url3 = "http://${ip.ip}/settext?closetime=${nextdate}"
        var ee = Executors.newSingleThreadExecutor()
        var get = HttpGetTask(url3)
        var f2 = ee.submit(get)
        try {
            var re = f2.get(60, TimeUnit.SECONDS)
            status = "Set text ${nextdate}"
        } catch (e: Exception) {
            logger.error("Call url 3 error ${e.message}")
            status = "Call url 3 error ${e.message}"
            throw Exception("call2")
        }
    }

    fun old(ip: Iptableskt, portname: String, value: Int, runtime: Int?, waittime: Int?) {
        val url = "http://${ip.ip}/run?port=${portname}&value=${value}&delay=${runtime}&waittime=${waittime}"
        logger.debug("Call to ${url}")
        val get = HttpGetTask(url)
        var ee = Executors.newSingleThreadExecutor()
        val f = ee.submit(get)
        try {
            val value = f.get(15, TimeUnit.SECONDS)
            logger.debug("setremote result ${value}")
            if (runtime != null) {
                status = "Run time state ${runtime}"
                logger.debug("Run time state ${runtime}")
                TimeUnit.SECONDS.sleep(runtime.toLong()) //หยุดรอถ้ามีการกำหนดมา
            }

        } catch (e: Exception) {
            logger.error("Can not connect to traget device [${e.message}]")
            ee.shutdownNow()
        }

        if (waittime != null) {
            status = "Wait time state ${waittime}"
            logger.debug("Wait time state ${waittime}")
            TimeUnit.SECONDS.sleep(waittime.toLong()) //หยุดรอถ้ามีการกำหนดมา
        }
    }

    fun callNewfunction(port: Portstatusinjob, ip: Iptableskt, runtime: Int?, waittime: Int?) {
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
                status = "call url 2 error ${e.message}"
                ee.shutdownNow()
                throw Exception("call1")
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
                status = "Call url 3 error ${e.message}"
                throw Exception("call2")
            }

            if (runtime != null) {
                status = "Run time ${runtime}"
                logger.debug("Run time ${runtime}")
                TimeUnit.SECONDS.sleep(runtime.toLong()) //หยุดรอถ้ามีการกำหนดมา
            }
            if (waittime != null) {
                status = "Wait time  ${waittime}"
                logger.debug("Wait time  ${waittime}")
            }
            logger.debug("End set new port ")
        } catch (e: Exception) {
            logger.error("Call new function ${e.message}")
            status = "Error new function ${e.message}"
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
                    var dsv = readvalue.readKtype(p)
                    v = dsv?.t
                } catch (e: Exception) {
                    logger.error("Read Tmp error ${e.message}")
                    status = "Read Tmp error ${e.message}"
                }
                logger.debug("Wait for high ${v} >= ${high}")
                status = "Wait for high ${v} >= ${high}"
                if (v != null) {
                    val tt = v.toFloat()
                    logger.debug("Check HIGH ${tt} > = ${high}")
                    status = "Check HIGH ${tt} > = ${high}"
                    if (tt >= high!!) {
                        logger.debug("This job can run now")
                        status = "This job can run now"
                        return true
                    } else {
                        logger.debug("This job have to wait")
                    }
                }
                status = "Wait ${timeout}"
                TimeUnit.SECONDS.sleep(1) // รอ 1 วินาที
                timeout--
                logger.debug("Check high timeout ${timeout}")
                if (timeout <= 0) {
                    status = "Check high time out"
                    logger.error("Check high time out")
                    isRun = false
                    throw Exception("Check high time out")
                }
            }
        } catch (e: Exception) {
            logger.error("Check high  ${e.message}")
            status = "Check high ${e.message}"
            isRun = false
            throw e
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
