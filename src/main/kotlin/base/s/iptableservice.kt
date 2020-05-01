package me.pixka.kt.base.s

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.r.IptablesktRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*


@Service
class IptableServicekt(val r: IptablesktRepo) : Ds<Iptableskt>() {

    override fun search(search: String, page: Long, limit: Long): List<Iptableskt>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun findByMac(mac: String): Iptableskt? {
        return r.findByMac(mac)
    }

    fun updateiptable(iptable: Iptableskt, ip: String): Iptableskt? {
        try {
            if (!iptable.ip.equals(ip)) {
                println("Save ip")
                iptable.ip = ip
                logger.debug("Save iptables ${iptable}")
                return save(iptable) as Iptableskt
            } else {
                iptable.lastcheckin = Date()
                return this.save(iptable) as Iptableskt
            }

        } catch (e: Exception) {
            logger.error("Update ip tables: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    fun resetAll() = r.deleteAll()

    companion object {
        internal var logger = LoggerFactory.getLogger(IptableServicekt::class.java)
    }
}