package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.util.*
import java.util.concurrent.TimeUnit

@Profile("pi")
class DSOTHERWorker(var pijob: Pijob,
                    var gpio: GpioService,
                    val ms: MessageService,val ps:PortstatusinjobService) : Runnable, PijobrunInterface {
    override fun state(): String? {
        return state
    }

    override fun startRun(): Date? {
        return startRun
    }
    var state:String? =" Crate "

    var startRun: Date?=null


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
            startRun = Date()
            state = " Star Run "+Date()
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
                    //pijob.ports
            var runtime = pijob.runtime
            var waittime = pijob.waittime
            jobid = pijob.id

            logger.debug("Startworker ${pijob.id}")
            ms.message("Start DSOTER Worker", "info")
            try {
                state = "Start set port"
                setport(ports!!)
            } catch (e: Exception) {
                state = "Set port error ${e.message}"
                logger.error("Set port error ${e.message}")
            }
            state = " Run time : ${runtime}"
            TimeUnit.SECONDS.sleep(runtime!!)
            logger.debug("Run time: ${runtime}")
            try {
                state = "Reset port"
                resetport(ports!!)
            } catch (e: Exception) {
                logger.error("Error reset Port ${e.message}")
            }
            state = " Wait time: ${waittime}"
            TimeUnit.SECONDS.sleep(waittime!!)
            logger.debug("Wait time: ${waittime}")

            //end task

            logger.debug("End job DSOTHER ${pijob.id}")
            ms.message("End DSOTER Worker", "info")
        } catch (e: Exception) {
            logger.error("DSOTHER ${e.message}")
        }

        state = " Run complate "
        isRun = false
    }

    fun resetport(ports: List<Portstatusinjob>) {
        for (p in ports) {
            var pin = gpio.getDigitalPin(p.portname?.name!!)
            gpio.resettoDefault(pin!!)
        }
    }

    fun setport(ports: List<Portstatusinjob>) {

        try {
            logger.debug("Gpio : ${gpio}")

            for (port in ports) {
                logger.debug("Port for pijob ${port}")
                var pin = gpio.getDigitalPin(port.portname?.name!!)
                logger.debug("Pin current state: ${pin}")

                //save old state
                // var b = Pinbackup(pin!!, pin.state)
                // pinbackuplist.add(b)

                var sn = port.status?.name
                if (sn?.indexOf("low") != -1) {

                    gpio.setPort(pin!!, false)
                    //pin.setState(false)
                } else {
                    gpio.setPort(pin!!, true)
                    //pin.setState(true)
                }
                logger.debug("Set pin state: ${pin.state}")

            }
        } catch (e: Exception) {
            logger.error("Error in set port ${e.message}")
            throw e
        }
    }

    override fun toString(): String {
        return "DSOTHERWorker id : ${pijob.id} Run: ${isRun}  ${pijob}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DSOTHERWorker::class.java)
    }
}