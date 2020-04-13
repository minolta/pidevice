package me.pixka.pibase.s

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Portname
import me.pixka.pibase.r.PortnameRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PortnameService( val r: PortnameRepo) : DefaultService<Portname>() {


    fun searchMatch(n: String): Portname? {
        return r.findByName(n)
    }

    fun findorcreate(n: String): Portname? {
        try {
            var pn: Portname? = this.searchMatch(n)
            if (pn == null) {
                pn = Portname()
                pn.name = n
                pn = save(pn)
            }
            return pn
        } catch (e: Exception) {
            logger.error("[findorcreate PortnameService] " + e.message)
            e.printStackTrace()
        }

        return null
    }

    fun getPinOutput(gpio: GpioController, name: String): GpioPinDigitalOutput {

        return gpio.getProvisionedPin(name) as GpioPinDigitalOutput

    }

    fun findByRefid(id: Long?): Portname? {
        try {
            return r.findByRefid(id)
        } catch (e: Exception) {
            logger.error("findByrefid PortnameService  : error " + e.message)
            e.printStackTrace()
        }

        return null
    }

    fun create(item: Portname): Portname {

        val p = Portname()
        p.refid = item.id
        p.name = item.name
        p.piport = item.piport
        return p
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(PortnameService::class.java!!)
    }

}
