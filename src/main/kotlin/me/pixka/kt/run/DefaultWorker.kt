package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import java.util.*


abstract class DefaultWorker(var pijob: Pijob, var gpios: GpioService,
                             var readUtil: ReadUtil, val ps: PortstatusinjobService, var logger: org.slf4j.Logger)
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
            return ps.findByPijobid(pijob.id) as List<Portstatusinjob>
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun resetport(ports: List<Portstatusinjob>?) {
        try {
            if (ports != null)
                for (port in ports) {
                    if (!port.status?.name.equals("check")) {
                        var pin = gpios.gpio.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                        logger.debug("Reset pin ${pin}")
                        status = "Reset pin ${pin}"
                        gpios.resettoDefault(pin)
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
                var pin = gpios.gpio.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput

                var sn = port.status?.name
                logger.debug("Set to " + sn)
                status = "Set to " + sn
                if (sn?.indexOf("low") != -1) {
                    gpios.setPort(pin, false)
                    //pin.setState(false)
                } else
                // pin.setState(true)
                    gpios.setPort(pin, true)


                logger.debug("Set pin state: ${pin.state}")
                status = "Set pin state: ${pin.state}"


            }
        } catch (e: Exception) {
            logger.error("Set port ${e.message}")
            status = "Set port ${e.message}"
            throw e
        }
    }

}