package me.pixka.pibase.o

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalOutput

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.pibase.s.LogistateService
import me.pixka.pibase.s.PortnameService

class RunpijobCallable : Callable<Pijob> {
    var isIsrun = true
    var ls: LogistateService? = null

    var pijob: Pijob? = null
    var ports: List<*>? = null
    var gpio: GpioController? = null
    private var ps: PortnameService? = null

    @Throws(Exception::class)
    override fun call(): Pijob? {

        try {
            val runtime = pijob!!.runtime
            val waittime = pijob!!.waittime

            setport(ports as List<Portstatusinjob>?)

            try {
                logger.debug("[runpijob callable] Set port and wait runtime:" + runtime!!)
                TimeUnit.SECONDS.sleep(runtime)
            } catch (e: Exception) {
                logger.error("[runpijob settime ] " + e.message)
            }

            // rest port

            // จบการทำงาน
            resetport(ports as List<Portstatusinjob>?)

            try {
                logger.debug("[runpijob callable] Reset port and waittime:" + waittime!!)
                TimeUnit.SECONDS.sleep(waittime)
            } catch (e: Exception) {
                logger.error("[runpijob settime ] " + e.message)
            }

            logger.debug("[runpijob callable] Job done")
            isIsrun = false// ทำงานเสร็จแล้ว
            // pijob.setId(0L);
        } catch (e: Exception) {
            logger.debug("[runpijob callable] Error :" + e.message)
            e.printStackTrace()
            isIsrun = false
            // pijob.setId(0L);
        }

        logger.debug("[runpijob callable] end job")
        return pijob
    }

    private fun resetport(list: List<Portstatusinjob>?) {
        logger.debug("[runpijob] Set port ")

        for (psj in list!!) {
            try {
                logger.debug("[runpijob port]" + psj)
                val pin = gpio!!.getProvisionedPin(psj.portname!!.name) as GpioPinDigitalOutput
                logger.debug("[runpijob] Reset  Port : " + pin)
                pin.toggle()
            } catch (e: Exception) {
                logger.error("runpijob resetport() : " + e.message)
                e.printStackTrace()
            }

        }
    }

    /**
     * ทำการ set port ตามที่กำหนดใน pijob
     *
     * @param list
     */
    private fun setport(list: List<Portstatusinjob>?) {

        logger.debug("[runpijob setport] Set port number of ports :" + list!!.size)
        for (psj in list) {
            try {
                logger.debug("[runpijob setport]" + psj)
                val pin = ps!!.getPinOutput(gpio!!, psj.portname!!.name!!)
                val status = psj.status
                logger.debug("[runpijob setport] Port : $pin set status to :$status")
                ls!!.setPort(pin, status!!)
            } catch (e: Exception) {
                logger.error("[runpijob setport()]  " + e.message)
                e.printStackTrace()
            }

        }
    }

    override fun equals(obj: Any?): Boolean {

        if (obj is Pijob) {
            val o = obj as Pijob?

            if (this.pijob!!.id!!.toInt() == o!!.id!!.toInt())
                return true
        }

        if (obj is RunpijobCallable) {
            val o = obj as RunpijobCallable?

            if (o!!.pijob!!.id!!.toInt() == this.pijob!!.id!!.toInt())
                return true
        }

        return false
    }

    fun setPortService(ps: PortnameService) {
        this.ps = ps
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunpijobCallable::class.java!!)
    }
}
