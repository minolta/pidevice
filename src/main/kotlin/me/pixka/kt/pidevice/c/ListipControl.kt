package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
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