package me.pixka.kt.pibase.s

import com.pi4j.io.gpio.*
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile("pi", "lite")
class GpioService(val gpio: GpioController) {

    var ports = ArrayList<Portstatus>()
    var in23value = 0
    var in22value = 0

    constructor() : this(gpio = GpioFactory.getInstance()) {
        logger.info("New GPIO Service")

        /**
         * สำหรับเปิด DELAY ต้อง HIGH ก่อน
         */

        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "p0", PinState.LOW), "p0")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "p1", PinState.LOW), "p1")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "p2", PinState.LOW), "p2")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "p3", PinState.LOW), "p3")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "p4", PinState.LOW), "p4")

        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "p5", PinState.LOW), "p5")
        //addToports(gpio.provisionDigitalInputPin(RaspiBcmPin.GPIO_05, "p5"), "p5")

        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "p21", PinState.HIGH), "p21")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "p22", PinState.LOW), "p22")
        /*
        //เอาไปใช้ เป็น output
        var in22 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22, "p22")
        addToports(in22, "p22")
        in22value = in22.state.value
        in22.addListener(GpioPinListenerDigital { event ->
            // display pin state on console
            println(" --> GPIO PIN STATE CHANGE: " + event.pin + " = " + event.state)
            this.in22value = event.state.value
        })
        */

        //addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "p23", PinState.LOW), "p23")
        var in23 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, "p23")
        in23value = in23.state.value
        addToports(in23, "p23")
        /*
        in23.addListener(GpioPinListenerDigital { event ->
            // display pin state on console
            logger.debug(" --> GPIO PIN STATE CHANGE: " + event.pin + " = " + event.state)
            this.in23value = event.state.value
        })*/

        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "p24", PinState.LOW), "p24")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "p25", PinState.LOW), "p25")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "p26", PinState.LOW), "p26")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "p27", PinState.LOW), "p27")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "p28", PinState.LOW), "p28")
        addToports(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "p29", PinState.LOW), "p29")
    }

    fun addToports(pin: GpioPinDigital, name: String) {
        var pi = pin as GpioPinDigital
        var p = Portstatus(pi, false, name)
    }

    fun getDigitalPin(pinname: String): GpioPinDigitalOutput? {
        try {
            logger.debug("Get Digital pin ${pinname}")
            if (gpio != null) {
                var pin = gpio.getProvisionedPin(pinname) as GpioPinDigitalOutput
                logger.debug("Get pin ${pin}")
                return pin
            }
        } catch (e: Exception) {
            logger.error("Get pin Error: ${e.message}")
        }
        return null
    }

    fun unlockport(name: String) {
        var port = findstatus(name)
        if (port != null) {
            port.inuse = false //unlock port
        }
    }

    fun resettoDefault(pin: GpioPinDigitalOutput) {
        try {

            logger.info(" toresetport " + pin.name)
            logger.debug(" toresetport " + pin.name)
            if (pin.name.equals("p0")) {
                setPort(pin, false)
            } else if (pin.name.equals("p1")) {
                setPort(pin, false)
            } else if (pin.name.equals("p2")) {
                setPort(pin, false)
            } else if (pin.name.equals("p3")) {
                setPort(pin, false)
            } else if (pin.name.equals("p4")) {
                setPort(pin, false)
            } else if (pin.name.equals("p5")) {
                setPort(pin, false)
            } else if (pin.name.equals("p21")) {
                logger.debug(" toresetportto  p21 to true")
                setPort(pin, true)
            } else if (pin.name.equals("p22")) {
                setPort(pin, false)
                logger.debug(" toresetportto  p22 to true")
            } else {
                logger.debug("${pin} toresetportto false")
                setPort(pin, false)
            }
            unlockport(pin.name)


        } catch (e: Exception) {
            logger.error("Error ${e.message}")
            throw e
        }


    }

    /**
     * ใช้สำหรับ check ว่า port ว่างอยู่เปล่า
     */
    fun checkPort(pin: GpioPinDigitalOutput): Portstatus? {
        var pn = pin.name

        var port = findstatus(pn)

        if (port != null) {
            if (!port.inuse)
                return port //ถ้าไม่มีการใช้
        }
        return null//can not use

    }

    fun findstatus(name: String): Portstatus? {
        for (port in ports) {
            logger.debug("find Port name ${name} in port ${port.portname}")
            if (name.equals(port.portname)) {

                logger.debug("Found port ${port}")
                return port
            }
        }

        logger.error("")
        return null

    }

    /**
     * ใช้สำหรับ lock port สำหรับ thread
     */
    fun setPort(pin: GpioPinDigitalOutput, state: Boolean): Boolean {
        /*
         var portstatus = checkPort(pin)
         if (portstatus == null) {
             throw Exception("Port in use")
         }

         portstatus.inuse = true
     */

        logger.debug("setpin ${pin} to ${state}")
        logger.info("Set pin ${pin} to ${state}")
        pin.setState(state)
        if (state) {
            if (!pin.isHigh) {
                //            portstatus.inuse = false
                throw Exception("Can not set status ${pin} to ${state}")
            }
            return true
        }

        if (!pin.isLow) {
            //      portstatus.inuse = false
            throw Exception("Can not set status ${pin} to ${state}")
        }
        return true
    }

    fun readPort(pin: GpioPinDigitalInput): Int {
        return pin.state.value
    }

    fun readPort(name: String) : GpioPinDigitalInput? {
        var port = gpio.getProvisionedPin(name) as GpioPinDigitalInput
        return port
    }


    fun revertDigitalpin(pin: GpioPinDigitalOutput): Boolean {

        try {
            logger.debug("Current State pin ${pin} ${pin.state}")
            if (pin.isHigh) {

                setPort(pin, false)
                logger.debug("Set to Low ${pin.state}")


            } else {
                setPort(pin, true)

            }
        } catch (e: Exception) {
            logger.error("Revert pin (reset) ${pin} ${e.message}")
            throw e
        }

        return true
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(GpioService::class.java)
    }
}

class Portstatus(var pin: GpioPinDigital? = null, var inuse: Boolean = false, var portname: String = "")