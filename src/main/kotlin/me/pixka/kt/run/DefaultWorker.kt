package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Logistate
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


abstract class DefaultWorker(var pijob: Pijob, var gpios: GpioService?=null,
                             var readUtil: ReadUtil?=null,
                             val ps: PortstatusinjobService?=null, var logger: org.slf4j.Logger)
    : PijobrunInterface, Runnable {


    var status: String? = null
    var isRun: Boolean = false
    var startRun: Date? = null
    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        this.gpios = gpios
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {

        if (pijob == null)
            throw Exception("pijob is null")

        return pijob.id

    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return startRun
    }

    override fun state(): String? {
        return status
    }

    fun loadPorts(p: Pijob): List<Portstatusinjob>? {
        try {
            return ps?.findByPijobid(pijob.id) as List<Portstatusinjob>
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun resetport(ports: List<Portstatusinjob>?) {
        try {
            if (ports != null)
                for (port in ports) {
                    if (!port.status?.name.equals("check") && port.enable!!) {
                        var pin = gpios?.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                        logger.debug("Reset pin ${pin}")
                        status = "Reset pin ${pin}"
                        gpios?.resettoDefault(pin)
                        logger.debug("Reset Port to default")
                    }
                }
        } catch (e: Exception) {
            logger.error("Reset port ${e.message}")
            status = "Resetport ${e.message}"
            throw e
        }
    }


    fun setport(ports: List<Portstatusinjob>) {
        try {
            logger.debug("Start set port ${Date()}")
            status = "Start set port ${Date()}"
            for (port in ports) {
                if (!port.enable!!) {
                    logger.debug("Port is disable")
                    status = "Port is disable"
                    continue // ถ้าไม่ enable ก็ข้ามไปเลย
                }
                logger.debug("Port for pijob ${port}")
                status = "Port for pijob ${port}"
                var pin = gpios?.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput

                var sn = port.status?.name
                logger.debug("Set to " + sn)
                status = "Set to " + sn
                if (sn?.indexOf("low") != -1) {
                    gpios?.setPort(pin, false)
                    //pin.setState(false)
                } else
                // pin.setState(true)
                    gpios?.setPort(pin, true)


                logger.debug("Set pin state: ${pin.state}")
                status = "Set pin state: ${pin.state}"

            }
        } catch (e: Exception) {
            logger.error("Set port ${e.message}")
            status = "Set port ${e.message}"
            throw e
        }
    }

    fun logtoint(logic: Logistate): Int {
        try {
            if (logic.name?.toLowerCase().equals("high"))
                return 1

        } catch (e: Exception) {
            logger.error("logtoin ${e.message}")
        }
        return 0
    }

    /**
     * สำหรับ D1
     */
   open fun setRemoteport(ports: List<Portstatusinjob>) {
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
                    var url = "http://${ip.ip}/run?port=${portname}&value=${value}&delay=${runtime}&waittime=${waittime}"
                    logger.debug("Call to ${url}")
                    var get = HttpGetTask(url)
                    var f = ee.submit(get)
                    try {
                        var value = f.get(3, TimeUnit.SECONDS)
                        logger.debug("Set remote result ${value}")
                        if(runtime!=null)
                        {
                            TimeUnit.SECONDS.sleep(runtime.toLong()) //หยุดรอถ้ามีการกำหนดมา
                        }
                        if(waittime!=null)
                        {
                            TimeUnit.SECONDS.sleep(waittime.toLong()) //หยุดรอถ้ามีการกำหนดมา
                        }
                    } catch (e: Exception) {
                        ee.shutdownNow()
                        logger.error("Can not connect to traget device [${e.message}]")
                    }
                }
            } catch (e: Exception) {
                logger.error("SetRemote ${e.message}")
            }
        }
    }

    fun setD1(portname: String?, ip: String) {

    }
}