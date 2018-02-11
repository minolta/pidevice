package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinState
import me.pixka.kt.pibase.s.GpioService
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.d.Portstatusinjob
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit

/**
 * ใช้สำหรับ Run pijob
 */
@Profile("pi")
class Worker(var pijob: Pijob, var gpio: GpioService) : Runnable, PijobrunInterface {
    override fun getPijobid(): Long {
        return pijob.id
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
            var ports = pijob.ports
            var runtime = pijob.runtime
            var waittime = pijob.waittime
            jobid = pijob.id

            logger.debug("Startworker ${pijob.id}")

            setport(ports!!)
            TimeUnit.SECONDS.sleep(runtime!!)
            logger.debug("Run time: ${runtime}")
            resetport(ports)
            TimeUnit.SECONDS.sleep(waittime!!)
            logger.debug("Wait time: ${waittime}")
            //end task

            logger.debug("End job ${pijob.id}")
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

    fun setport(ports: List<Portstatusinjob>) {
        try {
            logger.debug("Gpio : ${gpio}")

            for (port in ports) {
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
