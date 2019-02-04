package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ใช้สำหรับ Run pijob
 */
@Profile("pi", "lite")
open class Worker(var pijob: Pijob, var gpio: GpioService, val io: Piio, val ps: PortstatusinjobService)
    : Runnable, PijobrunInterface {
    override fun state(): String? {
        return state
    }

    override fun startRun(): Date? {
        return startrun
    }

    var state: String? = " Create "
    var startrun: Date? = null
    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun setG(gpios: GpioService) {
        gpio = gpios
    }


    override fun setP(p: Pijob) {
        pijob = p
    }

    var pinbackuplist = ArrayList<Pinbackup>()
    var jobid: Long = 0
    var isRun: Boolean = true
    override fun run() {
        try {
            isRun = true
            startrun = Date()
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>


            var porttocheck = findcheckport(ports)


            var runtime = pijob.runtime
            var waittime = pijob.waittime
            jobid = pijob.id
            var loop = 1
            if (pijob.timetorun != null) {
                if (pijob.timetorun!!.toInt() > 0)
                    loop = pijob.timetorun!!.toInt()
            }
            logger.debug("Startworker JOBID:${pijob.id} ${pijob.name}")
            //  ms.message("Start work id:${pijob.id} ", "info")


            var i = 0
            while (i < loop) {

                var canrun = true

                var value = 0
                if (porttocheck != null && porttocheck.size > 0) {
                    logger.debug("readport port to check ${porttocheck.size}")
                    for (port in porttocheck) {
                        var p = gpio.readPort(port.portname?.name!!)
                        logger.debug("readport ${p} ${p!!.state}")
                        if (p.isHigh) {
                            value = 1
                        } else {
                            value = 0
                            state = "Port not ready to run is 0"
                            canrun = false
                        }
                        logger.debug("Check port is ${value}")
                        state = "Check port is ${value}"
                    }
                }

                if (canrun) {
                    try {
                        state = "Run Set  ports"
                        setport(ports)
                    } catch (e: Exception) {
                        logger.error("Set port error ${e.message}")
                        state = "Error set port ${e.message}"
                    }
                    state = " Run Time ${runtime} Loop ${i}"
                    TimeUnit.SECONDS.sleep(runtime!!)
                    logger.debug("Run time: ${runtime}")
                    try {
                        state = " Reset port"
                        resetport(ports)
                    } catch (e: Exception) {
                        logger.error("Error reset PORT ${e.message}")
                        state = "Error reset port ${e.message}"
                        //      ms.message("Error : ${e.message}", "error")
                    }


                    state = "Wait ${waittime} Loop ${i}"
                } else
                    state = "Not Check port is low and wait LOOP:${i}"

                TimeUnit.SECONDS.sleep(waittime!!)
                logger.debug("Wait time: ${waittime} Loop ${i}")
                state = "Wait time: ${waittime} Loop ${i}"
                //end task
                logger.debug("End job ${pijob.id}")
                //  ms.message("End job ${pijob.id}", "info")


                i++

                logger.debug("Loop ${i}")
            }


        } catch (e: Exception) {
            logger.error("WOrking :${e.message}")
            state = "Error ${e.message}"
        }




        state = "End job"

        isRun = false
    }


    //ใช้สำหรับ check port
    fun findcheckport(ports: List<Portstatusinjob>): ArrayList<Portstatusinjob> {
        var tochecks = ArrayList<Portstatusinjob>()
        for (port in ports) {
            var pn = port.status?.name?.toLowerCase()
            if (pn?.indexOf("check") != -1) {
                tochecks.add(port)
            }
        }
        return tochecks
    }

    fun resetport(ports: List<Portstatusinjob>?) {
        try {
            if (ports != null)
                for (port in ports) {
                    if (!port.status?.name.equals("check")) {
                        var pin = gpio.gpio.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                        logger.debug("Reset pin ${pin}")
                        state = "Reset pin ${pin}"
                        gpio.resettoDefault(pin)
                        logger.debug("Reset Port to default")
                    }
                }
        } catch (e: Exception) {
            logger.error("Reset port ${e.message}")
            state = "Resetport ${e.message}"
            throw e
        }
    }

    fun findRuntime(port: Portstatusinjob): Int? {
        try {
            var rt = port.runtime
            if (rt == null || rt.toInt() == 0)
                return null
            return rt
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }


    }

    fun findWaittime(port: Portstatusinjob): Int? {
        try {
            var rt = port.waittime
            if (rt == null || rt.toInt() == 0)
                return null
            return rt
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    open fun setport(ports: List<Portstatusinjob>) {
        try {
            logger.debug("Gpio : ${gpio}")
            for (port in ports) {
                if (port.enable == null || port.enable == false || port.status?.name.equals("check"))
                {//ถ้า Enable == null หรือ false ให้ไปทำงาน port ต่อไปเลย
                    logger.error("Not set port ${port}")
                } else {


                    logger.debug("Port for pijob ${port}")
                    var pin = gpio.gpio.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                    logger.debug("Pin: ${pin}")

                    //save old state
                    //  var b = Pinbackup(pin, pin.state)
                    //   pinbackuplist.add(b)

                    var sn = port.status?.name
                    logger.debug("Set to " + sn)
                    if (sn?.indexOf("low") != -1) {
                        gpio.setPort(pin, false)
                        //pin.setState(false)
                    } else
                    // pin.setState(true)
                        gpio.setPort(pin, true)


                    logger.debug("Set pin state: ${pin.state}")


                }
            }
        } catch (e: Exception) {
            logger.error("Set port ${e.message}")
            throw e
        }
    }

    override fun toString(): String {
        return "Work pijob id : ${pijob.id} Run: ${isRun}  ${pijob}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Worker::class.java)
    }


    fun runPorts(pijob: Pijob) {
        var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
        logger.debug("Start run port ${ports}")
        var runtime = pijob.runtime
        var nextrun = pijob.waittime
        if (ports.size > 0) {
            setport(ports)
            if (runtime != null)
                TimeUnit.SECONDS.sleep(runtime)
            resetport(ports)

            if (nextrun != null)
                TimeUnit.SECONDS.sleep(nextrun)


            logger.debug("Run port is end")


        }

    }
}

class Pinbackup(var pin: GpioPinDigitalOutput, var pinstate: PinState)
