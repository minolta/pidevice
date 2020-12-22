package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.c.Statusobj
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class MactoipService(
    val ips: IptableServicekt, val lgs: LogService, val http: HttpService,
    val dhts: ReadDhtService, val psis: PortstatusinjobService,
    val dizs: DeviceinzoneService
) {
    val om = ObjectMapper()
    fun mactoip(mac: String): String? {
        try {
            var ip = ips.findByMac(mac)
            if (ip != null)
                return ip.ip
            lgs.createERROR(
                "IP Not found for ${mac}", Date(), "MacToip", "",
                "14", "mactoip", "${mac}"
            )
            throw Exception("IP Not found for ${mac}")
        } catch (e: Exception) {
            logger.error("Mac to ip:${e.message} ")
            lgs.createERROR(
                "${e.message}", Date(), "MacToip", "",
                "14", "mactoip", "${mac}"
            )

//            throw e
        }

        return null

    }

    fun openpump(pijob: Pijob, timetoopen: Int): String {
        var ok = true
        var error = ""
        try {
            var id = pijob.pijobgroup?.id

            if (id != null) {
                var devices = dizs.deviceinszone(id!!)

                if (devices != null) {
                    devices.forEach {
                        var ip = mactoip(it.pidevice?.mac!!)
                        try {
                            var re = http.getNoCache("http://${ip}/run?delay=${timetoopen}", 15000)
                            var status = om.readValue<Statusobj>(re)
                        } catch (e: Exception) {
                            logger.error("openpump ERROR ON PUMP ${e.message}")
                            error = "openpump ERROR ON PUMP ${e.message}"
                            ok = false
                        }
                    }
                    if (ok)
                        return "Open pump ok"
                    else
                        return error
                } else {
                    logger.warn("openpump Not pump in this zone to open")
                    return "openpump Not pump in this zone to open"

                }
            } else {
                logger.error("openpump Zone id is null")
                return "openpump Zone id is null"
            }
        } catch (e: Exception) {
            logger.error("openpump ERROR ${e.message}")
            return "openpump ERROR ${e.message}"
        }
    }

    //สำหรับค้นหาว่าใช้เวลาเท่าไหร่
    fun findTimeofjob(pijob: Pijob): Int {

        try {
            var ports = getPortstatus(pijob)
            var r = 0
            var w = 0
            ports?.forEach {

                if (it.waittime != null)
                    w += it.waittime!!
                if (it.runtime != null)
                    r += it.runtime!!

            }
            return w + r
        } catch (e: Exception) {
            logger.error("findtimeofjob ERROR ${e.message}")
            throw e
        }


    }

    fun readTmp(pijob: Pijob, timeout: Int = 2000): BigDecimal? {
        try {
            val ip = mactoip(pijob.desdevice?.mac!!)
            if (ip != null) {
                val to: Tmpobj
                try {
                    var re = http.getNoCache("http://${ip}", timeout)
                    var t = om.readValue<Tmpobj>(re)
                    if (t.tmp != null)
                        return t.tmp

                    if (t.t != null)
                        return t.t

                    return BigDecimal(-127)
                } catch (e: Exception) {
                    logger.error("Read tmp service ERROR ${e.message}")
                    throw e
                }

            }
            throw Exception("Not found ip")
        } catch (e: Exception) {
            logger.error("Read Tmp ${e.message} MAC:${pijob.desdevice?.mac}")
            throw e
        }
    }

    fun getPortstatus(job: Pijob): List<Portstatusinjob>? {
        try {
            return psis.findByPijobid(job.id) as List<Portstatusinjob>?
        } catch (e: Exception) {
            logger.error("Get portStatus ERROR ${e.message}")
            throw e
        }
    }

    fun readStatus(job: Pijob): String {
        var ip: String? = null
        try {
            ip = mactoip(job.desdevice?.mac!!)
        } catch (e: Exception) {
            logger.error("Read status ERROR ${e.message} ${job.name}")
            throw e
        }
        try {
            var result = http.get("http://${ip}", 5000)
            return result
        } catch (e: Exception) {
            logger.error("Get Status ERROR ${e.message} ${job.name}")
            throw e
        }
    }

    fun findUrl(
        target: PiDevice, portname: String, runtime: Long,
        waittime: Long, value: Int
    ): String {
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
            var url = findUrl(
                portstatusinjob.device!!, portstatusinjob.portname!!.name!!,
                portstatusinjob.runtime!!.toLong(), portstatusinjob.waittime!!.toLong(),
                portstatusinjob.status!!.toInt()
            )
            return http.getNoCache(url, 15000)
        } catch (e: Exception) {
            logger.error("Set port :" + e.message)
            lgs.createERROR(
                "${e.message}", Date(),
                "MactoipService", Thread.currentThread().name, "51",
                "setport()", portstatusinjob.device!!.mac, portstatusinjob.pijob!!.refid,
                portstatusinjob.pijob!!.pidevice?.refid
            )

            throw  e
        }
    }


    var logger = LoggerFactory.getLogger(MactoipService::class.java)
}