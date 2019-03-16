package me.pixka.kt.run

import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class D1pjobWorker(var pijob: Pijob, val readUtil: ReadUtil)
    : PijobrunInterface, Runnable {
    var isRun = false
    var state = "Init"
    var startrun: Date? = null

    override fun setG(gpios: GpioService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun run() {
        startrun = Date()
        isRun = true
        try {
            var v = readUtil.readPressureByjob(pijob)
            logger.debug("D1 Found pressure : ${v}")
            if (v != null) {
                var value = v.pressurevalue?.toFloat()
                var h = pijob.hhigh?.toFloat()
                var l = pijob.hlow?.toFloat()
                logger.debug("D1 pressure  ${l} < ${value} > ${h}")
                if (checkH(l!!, h!!, value!!)) {
                    go()
                    isRun = false
                } else {
                    logger.error("D1 pressure job Value not in rang ${l} < ${value} > ${h}")
                    isRun = false
                }
            } else {
                state = "Not found pressure value"
                isRun = false
            }
        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR ${e.message}")
            throw e
        }

        isRun = false

    }


    fun checkH(l: Float, h: Float, v: Float): Boolean {
        state = "Check value ${l} < ${v} > ${h}"
        if (v >= l && v <= h) {
            return true
        }
        return false
    }

    fun go() {//Run
        state = "Run set port "
        var ports = pijob.ports
        logger.debug("Ports ${ports}")
        if (ports != null)
            for (port in ports) {


                var pw = port.waittime
                var pr = port.runtime
                var pn = port.portname!!.name
                var tg = port.device
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
                if (v != null) {
                    if (v.name.equals("high")) {
                        value = 1
                    } else value = 0
                }

                try {
                    var url = findUrl(tg!!, portname!!, runtime, waittime, value)
                    logger.debug("URL ${url}")
                    state = "Set port ${url}"
                    var get = HttpGetTask(url)
                    var ee = Executors.newSingleThreadExecutor()
                    var f = ee.submit(get)
                    var value = f.get(5, TimeUnit.SECONDS)
                    state = "Delay  ${runtime} + ${waittime}"
                    logger.debug("Value ${value}")
                    TimeUnit.SECONDS.sleep(runtime)
                    TimeUnit.SECONDS.sleep(waittime)
                } catch (e: Exception) {
                    logger.error("Error ${e.message}")
                    state = " Error ${e.message}"
                }

            }

        state = "Set port ok "

    }

    fun findUrl(tg: PiDevice, portname: String, runtime: Long, waittime: Long, value: Int): String {
        if (pijob.desdevice != null) {
            var ip = readUtil.findIp(tg)
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
        internal var logger = LoggerFactory.getLogger(D1pjobWorker::class.java)
    }
}