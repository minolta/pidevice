package me.pixka.kt.run

import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class D1hjobWorker(var pijob: Pijob, var gpios: GpioService, val dhtvalueService: DhtvalueService,
                   val dhts: Dhtutil, val httpControl: HttpControl)
    : PijobrunInterface, Runnable {
    var isRun = false

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun state(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun run() {

        isRun = true
        try {
            var dhtvalue = dhts.readByPijob(pijob)
            logger.debug("DHTVALUE ${dhtvalue}")
            if (dhtvalue != null) {

                if (checkH(pijob.hlow?.toFloat()!!, pijob.hhigh?.toFloat()!!, dhtvalue.h?.toFloat()!!)) {
                    logger.debug("Go!!")
                    go()
                    isRun = false
                }

            }
        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR ${e.message}")
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
                    httpControl.get(url)
                    TimeUnit.SECONDS.sleep(runtime)
                    TimeUnit.SECONDS.sleep(waittime)
                } catch (e: Exception) {
                    logger.error("Error ${e.message}")
                }

            }

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

    override fun setP(pijob: Pijob) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(D1hjobWorker::class.java)
    }
}