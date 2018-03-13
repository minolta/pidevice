package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.d.Portstatusinjob
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit
@Profile("pi","lite")
class HWorker(var pj: Pijob, var gi: GpioService, val m: MessageService, val i: Piio) : Worker(pj, gi,i) {

    override fun setport(ports: List<Portstatusinjob>) {
        var runtime = pijob.runtime
        try {
            logger.debug("Gpio : ${gpio}")

            for (port in ports) {

                if (port.enable != null && port.enable == true) {
                    logger.debug("Port for pijob ${port}")
                    var pin = gpio.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                    logger.debug("Pin: ${pin}")

                 //   m.message("Open Port ${pin} in ${runtime} sec ", "info")
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

                    TimeUnit.SECONDS.sleep(runtime!!) //หยุดรอที่ละ port
                    gpio.resettoDefault(pin)
                }
            }
        } catch (e: Exception) {
            logger.error("Set port ${e.message}")
            throw e
        }
    }
}