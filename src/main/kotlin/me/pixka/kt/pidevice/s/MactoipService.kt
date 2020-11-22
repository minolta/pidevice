package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.worker.D1TWorkerII
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class MactoipService(val ips: IptableServicekt, val lgs: LogService, val http: HttpService,
                     val dhts: ReadDhtService, val psis: PortstatusinjobService,
                     val readTmpService: ReadTmpService) {
    val om = ObjectMapper()
    fun mactoip(mac: String): String? {
        try {
            var ip = ips.findByMac(mac)
            if (ip != null)
                return ip.ip
            lgs.createERROR("IP Not found for ${mac}", Date(), "MacToip", "",
                    "14", "mactoip", "${mac}")
            throw Exception("IP Not found for ${mac}")
        } catch (e: Exception) {
            logger.error("Mac to ip:${e.message} ")
            lgs.createERROR("${e.message}", Date(), "MacToip", "",
                    "14", "mactoip", "${mac}")

//            throw e
        }

        return null

    }

    fun readTmp(pijob: Pijob): BigDecimal? {
        try {
            val ip = mactoip(pijob.desdevice?.mac!!)
            if (ip != null) {
                val to:Tmpobj
                try {
                    to = readTmpService.readTmp(ip)
                }catch (e:Exception)
                {
                    logger.error("Read tmp service ERROR ${e.message}")
                    throw e
                }
                if (to.tmp != null)
                    return to.tmp
                if (to.t != null)
                    return to.t

                return BigDecimal(-127)
            }
            throw Exception("Not found ip")
        } catch (e: Exception) {
            logger.error("Read Tmp ${e.message} MAC:${pijob.desdevice?.mac}")
            throw e
        }
    }

    fun getPortstatus(job: Pijob): List<Portstatusinjob>? {
        return psis.findByPijobid(job.id) as List<Portstatusinjob>?
    }

    fun readStatus(job: Pijob): String {
        var ip: String? = null
        try {
            ip = mactoip(job.desdevice?.mac!!)
        } catch (e: Exception) {
            logger.error("Read status ERROR ${e.message}")
            throw e
        }
        try {
            var result = http.get("http://${ip}",5000)
            return result
        } catch (e: Exception) {
            logger.error("Get Status ERROR ${e.message}")
            throw e
        }
    }

    fun findUrl(target: PiDevice, portname: String, runtime: Long,
                waittime: Long, value: Int): String {
        try {
            var ip = ips.findByMac(target.mac!!)
            if (ip != null) {
                var url = "http://${ip.ip}/run?port=${portname}&delay=${runtime}&value=${value}&wait=${waittime}"
                return url
            }
            throw Exception("Ip not found")
        } catch (e: Exception) {
            logger.error("Find URL" + e.message)
            throw e
        }
    }

    //สำหรับ setport
    fun setport(portstatusinjob: Portstatusinjob): String {
        try {
            var url = findUrl(portstatusinjob.device!!, portstatusinjob.portname!!.name!!,
                    portstatusinjob.runtime!!.toLong(), portstatusinjob.waittime!!.toLong(),
                    portstatusinjob.status!!.toInt())
            return http.getNoCache(url, 15000)
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "MactoipService", Thread.currentThread().name, "51",
                    "setport()", portstatusinjob.device!!.mac, portstatusinjob.pijob!!.refid,
                    portstatusinjob.pijob!!.pidevice?.refid)
            logger.error("Set port :" + e.message)
            throw  e
        }
    }


    var logger = LoggerFactory.getLogger(MactoipService::class.java)
}