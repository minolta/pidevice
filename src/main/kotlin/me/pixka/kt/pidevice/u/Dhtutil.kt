package me.pixka.kt.pidevice.u

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class Dhtutil(val http: HttpControl, val ips: IptableServicekt, val ntfs: NotifyService) {
    val om = ObjectMapper()
    fun readDhtfromOther(url: String): Dhtvalue? {
        var ee = Executors.newSingleThreadExecutor()
        var get = HttpGetTask(url)
        try {
            var f = ee.submit(get)
            var rep: String? = null

            try {
                rep = f.get(15, TimeUnit.SECONDS)
//                rep = URL(url).readText()
            } catch (e: Exception) {
                logger.error("GetDHT DHTUTIL value ERROR ${e.message} ${e} ${url}")
//                ntfs.error("GetDHT DHTUTIL value ERROR ${e.message} ${e} ${url}")
                ee.shutdownNow()
                throw Exception("GetDHT value ERROR ${e.message} ${e}")
            }
            var dhtvalue = om.readValue<Dhtvalue>(rep, Dhtvalue::class.java)

            return dhtvalue
        } catch (e: Exception) {
            logger.error("line 56 ${e.message} ${url}")

//            ntfs.error("GetDHT DHTUTIL value ERROR ${e.message} ${e} ${url}")
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
            logger.error("Read dht error ${e.message}")
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