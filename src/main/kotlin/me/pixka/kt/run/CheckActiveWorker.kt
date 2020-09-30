package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class CheckActiveWorker(var pijob: Pijob, val ps: PortstatusinjobService, val httpService: HttpService,
                        val ips: IptableServicekt, val ntfs: NotifyService, var lgs: LogService)
    : PijobrunInterface, Runnable {
    companion object {
        internal var logger = LoggerFactory.getLogger(CheckActiveWorker::class.java)
    }

    var exitdate: Date? = null
    val om = ObjectMapper()
    var isRun = false
    var state = "Init"
    var startrun: Date? = null
    override fun setP(p: Pijob) {
        this.pijob = p
    }

    override fun setG(gpios: GpioService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return startrun
    }

    override fun state(): String? {
        return state
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }

    fun check(ip: String, token: String? = null, device: PiDevice): Boolean {
        logger.debug("checkactive ${ip}")
        try {
            var re = httpService.get("http://${ip}",2000)
            var status = om.readValue<Status>(re, Status::class.java)
            state = "Device:${device.name} uptime :${status.uptime} ative ok."
            if (status.errormessage != null) {
                //ถ้า มี ERROR MESSAGE ให้ ส่งเข้า line เลย
                if (status.errormessage?.isNotEmpty()!!) {
                    lgs.createERROR("Have ERROR  ${device.name} ${pijob.name} ${status.errormessage}", Date(),
                            "CheckActiveWoterk", pijob.name, "72", "",
                            "${device.mac}",pijob.refid)
                    if (token != null)
                        ntfs.message("Have ERROR  ${device.name} ${pijob.name} ${status.errormessage}", token)
                    else
                        ntfs.message("Have ERROR ${device.name} ${pijob.name}  ${status.errormessage}")
                    return false
                }
            }
        } catch (e: Exception) {
            logger.error("Error  ${pijob.name}  ${device.name} ${e.message}")
            lgs.createERROR("Error  ${pijob.name}  ${device.name} ${e.message}", Date(),
                    "Checkactive", "83", "", "", "${device.mac}",
            pijob.refid)
            if (token != null)
                ntfs.message("device not respone ${device.name} ${pijob.name} ${e.message}", token)
            else
                ntfs.message("device not respone ${device.name} ${pijob.name}  ${e.message}")
            return false
        }
        return true
    }

    fun setEnddate() {
        var t = 0L
        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            isRun = false//ออกเลย
    }

    override fun run() {
        isRun = true
        startrun = Date()
        logger.debug(" checkactive Start run ${startrun} ${pijob.name}")
        var token = pijob.token
        var mac: String? = null

        try {
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
            logger.debug("Found port ${ports.size} ${pijob.name}")


            for (port in ports) {

                if (port.enable != null && port.enable!!) {
                    val device = port.device
                    mac = device?.mac
                    logger.debug("Find device ip ${device} mac ${device!!.mac} ${pijob.name}")
                    var ip = ips.findByMac(device.mac!!)

                    if (ip != null) {
                        //check(ip.ip!!, token, device)
                        CompletableFuture.supplyAsync { check(ip.ip!!, token, device) }.thenApply {
                            logger.debug("Endcheck checkactive ${port.device?.name} ${ip}  ${Date()}")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            logger.error(e.message)
            lgs.createERROR("Error ${e.message} ${pijob.name}", Date(), "",
                    "", "", "", "${mac}",pijob.refid)
            state = "Error ${e.message} ${pijob.name}"
        }

//        isRun = false
        setEnddate()
        state = "End run wait exit date"
    }

    override fun toString(): String {
        return "CHECKACTIVE NAME ${pijob.name} ${startrun} ${state}"
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class Status(var message: String? = null, var ip: String? = null, var uptime: Long? = 0,
             var name: String? = null, var t: BigDecimal? = null, var h: BigDecimal? = null, var ssid: String? = null,
             var version: String? = null, var errormessage: String? = null, var status: String? = null,
             var pm25: BigDecimal? = null, var pm1: BigDecimal? = null, var pm10: BigDecimal? = null) {
    override fun toString(): String {
        return "${name} ${message} ${ssid} ${version} ${t} ${h} ${ip}"
    }
}
