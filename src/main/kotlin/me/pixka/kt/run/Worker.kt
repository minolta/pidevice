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
open class Worker(var pijob: Pijob, var gpio: GpioService, val io: Piio,val ps:PortstatusinjobService) : Runnable, PijobrunInterface {
    override fun state(): String? {
        return state
    }

    override fun startRun(): Date? {
        return startrun
    }
    var state:String?= " Create "
    var startrun : Date? = null
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
            var runtime = pijob.runtime
            var waittime = pijob.waittime
            jobid = pijob.id
            var loop = 1
            if (pijob.timetorun != null) {
                if (pijob.timetorun!!.toInt() > 0)
                    loop = pijob.timetorun!!.toInt()
            }
            logger.debug("Startworker ${pijob.id}")
            //  ms.message("Start work id:${pijob.id} ", "info")


            var i = 0
            while (i < loop) {


                try {
                    state = "Run Set  ports"
                    setport(ports!!)
                } catch (e: Exception) {
                    logger.error("Set port error ${e.message}")
                }
                state = " Run Time ${runtime}"
                TimeUnit.SECONDS.sleep(runtime!!)
                logger.debug("Run time: ${runtime}")
                try {
                    state = " Reset port"
                    resetport(ports)
                } catch (e: Exception) {
                    logger.error("Error reset PORT ${e.message}")
                    //      ms.message("Error : ${e.message}", "error")
                }
                state = "Wait ${waittime}"
                TimeUnit.SECONDS.sleep(waittime!!)
                logger.debug("Wait time: ${waittime}")
                //end task
                logger.debug("End job ${pijob.id}")
                //  ms.message("End job ${pijob.id}", "info")


                i++

                logger.debug("Loop ${i}")
            }


        } catch (e: Exception) {
            logger.error("WOrking :${e.message}")
        }






        isRun = false
    }

    fun resetport(ports: List<Portstatusinjob>?) {

        if (ports != null)
            for (port in ports) {
                logger.debug("Reset Port to default")
                var pin = gpio.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                gpio.resettoDefault(pin)
            }

    }

    open fun setport(ports: List<Portstatusinjob>) {
        try {
            logger.debug("Gpio : ${gpio}")


            for (port in ports) {

                if (port.enable == null || port.enable == false) {//ถ้า Enable == null หรือ false ให้ไปทำงาน port ต่อไปเลย

                    logger.debug("Not set port ${port}")
                } else {


                    logger.debug("Port for pijob ${port}")
                    var pin = gpio.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
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


}

class Pinbackup(var pin: GpioPinDigitalOutput, var pinstate: PinState)
