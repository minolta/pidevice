package me.pixka.kt.pidevice.c

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import org.springframework.web.bind.annotation.*

@RestController
class ListipControl(val iptableServicekt: IptableServicekt) {
    @CrossOrigin
    @RequestMapping(value = ["/ips"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): List<Iptableskt>? {

        return iptableServicekt.all()
    }

    @CrossOrigin
    @RequestMapping(value = ["/ipsreset"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun resetall(): Boolean {

        iptableServicekt.resetAll()

        return true
    }
}