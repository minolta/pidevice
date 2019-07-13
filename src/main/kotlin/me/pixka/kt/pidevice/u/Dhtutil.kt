package me.pixka.kt.pidevice.u

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class Dhtutil(val http: HttpControl, val ips: IptableServicekt) {
    val om = ObjectMapper()
    fun readDhtfromOther(url: String): Dhtvalue? {
        var ee = Executors.newSingleThreadExecutor()
        var get = HttpGetTask(url)

        try {
            var f = ee.submit(get)
            var rep = f.get(30, TimeUnit.SECONDS)
            var dhtvalue = om.readValue<Dhtvalue>(rep, Dhtvalue::class.java)
            return dhtvalue
        } catch (e: Exception) {
            logger.error("line 56 ${e.message} ${url}")
            ee.shutdownNow()

            throw e
        }
        return null
    }


    fun readFrommac(mac: String): Dhtvalue? {

        try {
            var ip = mactoip(mac)
            if (ip != null) {
                var url = "http://${ip.ip}/dht"
                var dhtvalue = readDhtfromOther(url)
                logger.debug("Read dht value ${dhtvalue} from ${url}")
                if (dhtvalue != null) {
                    return dhtvalue
                }
                throw Exception("Value is null")
            }
        } catch (e: Exception) {
            throw e
        }
        return null
    }

    fun readByPijob(pijob: Pijob): Dhtvalue? {

        if (pijob.desdevice != null) {
            return readFrommac(pijob.desdevice?.mac!!)
        }
        throw Exception("Not have des device ")


    }

    fun mactoip(mac: String): Iptableskt? {
        try {
            var ip = ips.findByMac(mac)
            return ip
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Dhtutil::class.java)
    }
}