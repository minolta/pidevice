package me.pixka.kt.run

import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class D1tjobWorker(var pijob: Pijob, var gpios: GpioService, val dhtvalueService: DhtvalueService,
                   val readvalue: ReadUtil, val httpControl: HttpControl)
    : PijobrunInterface, Runnable {
    var isRun = false
    var state = ""
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
            var ds18value = readvalue.readTfromD1Byjob(pijob)

            logger.debug("DS18value ${ds18value}")
            state = "DS18value [${ds18value}]"
            if (ds18value != null) {

                if (checkH(pijob.tlow?.toFloat()!!, pijob.thigh?.toFloat()!!, ds18value.t?.toFloat()!!)) {
                    logger.debug("Go!!")
                    go()
                    isRun = false
                }

            }
        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR ${e.message}")
            state = "ERROR ${e.message}"
            throw e
        }

        isRun = false

    }

    fun checkH(l: Float, h: Float, v: Float): Boolean {
        if (v >= l && v <= h) {
            return true
        }
        return false
    }

    fun go() {//Run
        var ports = pijob.ports
        logger.debug("Ports ${ports}")
        if (ports != null)
            for (port in ports) {


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
                if (v != null) {
                    if (v.name.equals("high")) {
                        value = 1
                    } else value = 0
                }

                try {
                    var url = findUrl(portname!!, runtime, waittime, value)
                    logger.debug("URL ${url}")
                    var get = HttpGetTask(url)
                    var ee = Executors.newSingleThreadExecutor()
                    var f = ee.submit(get)
                    var value = f.get(5, TimeUnit.SECONDS)
                    logger.debug("String from open [${value}]")
                    TimeUnit.SECONDS.sleep(runtime)
                    TimeUnit.SECONDS.sleep(waittime)
                } catch (e: Exception) {
                    logger.error("Error ${e.message}")
                    state = "ERROR set port ${e.message} "
                }

            }

    }

    fun findUrl(portname: String, runtime: Long, waittime: Long, value: Int): String {
        if (pijob.desdevice != null) {
            var ip = readvalue.findip(pijob.desdevice!!.mac!!)
            if (ip != null) {
                var url = "http://${ip}/run?port=${portname}&delay=${runtime}&value=${value}&wait=${waittime}"
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
}