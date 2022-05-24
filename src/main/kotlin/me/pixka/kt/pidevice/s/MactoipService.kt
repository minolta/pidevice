package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
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
    val dhts: ReadDhtService, val psis: PortstatusinjobService, val pds: PideviceService,
    val dizs: DeviceinzoneService, val pjs: PijobService, val pus: PumpforpijobService
) {
    val om = ObjectMapper()
    fun mactoip(mac: String): String? {
        try {
            var d = pds.findByMac(mac)

            var ip = d?.ip

            if (ip == null) {
                lgs.createERROR(
                    "IP Not found for ${mac}", Date(), "MacToip", "",
                    "14", "mactoip", "${mac}"
                )
                throw Exception("IP Not found for ${mac}")
            }
            return ip

        } catch (e: Exception) {
            logger.error("Mac to ip:${e.message} ")
            e.printStackTrace()
            lgs.createERROR(
                "${e.message}", Date(), "MacToip", "",
                "14", "mactoip", "${mac}"
            )

//            throw e
        }

        return null

    }

    fun readDistance(ip: String): Long? {
        try {
            var url = "http://${ip}"
            var re = http.get2Nocache(url)
            var s = om.readValue<Statusobj>(re!!)
            return s.distance
        }catch (e:Exception)
        {
            e.printStackTrace()
            throw e
        }
    }

    fun openpump(pijob: Pijob, timetoopen: Int): String {
        var ok = true
        var error = ""
        try {
            var id = pijob.pijobgroup?.id

            if (id != null) {
                var devices = dizs.deviceinszone(id)

                if (devices != null) {
                    devices.forEach {
                        var ip = it.pidevice?.ip

                        try {
                            var re = http.getNoCache("http://${ip}/run?delay=${timetoopen}", 5000)
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
                    throw Exception("openpump Not pump in this zone to open")
                }
            } else {
                logger.error("openpump Zone id is null")
                throw Exception("openpump Zone id is null")
            }
        } catch (e: Exception) {
            logger.error("openpump ERROR ${e.message}")
            throw e
        }
    }

    fun openpumps(pidevice: PiDevice, timetoopen: Int) {
        try {
            var re = http.getNoCache("http://${pidevice.ip}/run?delay=${timetoopen}", 15000)
            var status = om.readValue<Statusobj>(re)
        } catch (e: Exception) {
            logger.error("openpump ERROR ON PUMP ${e.message}")
            throw e
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

    fun readTmp(pijob: Pijob, timeout: Int = 20000): BigDecimal? {
        try {
            val ip = pijob.desdevice?.ip
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

    fun getPortstatus(job: Pijob, state: Boolean = true): List<Portstatusinjob>? {
        try {
            var p = psis.findByPijobid(job.id) as List<Portstatusinjob>?
            return p?.filter { it.enable == true }
        } catch (e: Exception) {
            logger.error("Get portStatus ERROR ${e.message}")
            throw e
        }
    }

    /**
     * เป็นการอ่าน status ของ des device
      */
    fun readStatus(job: Pijob, timeout: Int = 60000): String {
        var ip: String? = null
        try {
            ip = job.desdevice?.ip
        } catch (e: Exception) {
            logger.error("Read status ERROR ${e.message} ${job.name}")
            throw e
        }
        try {
            var result = http.get("http://${ip}", timeout)
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
    //สำหรับ setport
    fun setport(portstatusinjob: Portstatusinjob,timeout:Int): String {
        try {
            var url = findUrl(
                portstatusinjob.device!!, portstatusinjob.portname!!.name!!,
                portstatusinjob.runtime!!.toLong(), portstatusinjob.waittime!!.toLong(),
                portstatusinjob.status!!.toInt()
            )
            return http.getNoCache(url, timeout)
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


    fun readPressure(pidevice: PiDevice,timeout:Int=20000): Double? {
        var url = "http://${pidevice.ip}"
        var status:Statusobj?=null
        try {

            var result = http.getNoCache(url,timeout)
            status = om.readValue<Statusobj>(result)
            return status.psi
        } catch (e: Exception) {
            logger.error("Read Pressure()  URL:${url}  Status:${status} : " + e.message)
            throw e
        }
    }

    var logger = LoggerFactory.getLogger(MactoipService::class.java)


}