package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*


class DustWorker(job: Pijob, var ports: ArrayList<Portstatusinjob>,val mtp:MactoipService)
    :DWK(job), Runnable {
    var om = ObjectMapper()
    var totalrun = 0
    var totalwait = 0

    override fun run() {
        isRun = true
        startRun = Date()
        var mac = ""
        try {
            ports.filter { it.enable == true }.forEach {
                var ip = it.device?.ip
                mac = it.device?.mac!!
                if (ip != null) {
                    if (it.runtime != null && it.runtime!!.toInt() > totalrun)
                        totalrun = it.runtime!!.toInt()
                    if (it.waittime != null && it.waittime!!.toInt() > totalwait)
                        totalwait = it.waittime!!.toInt()
//                    var url = "http://${ip.ip}/run?port=${it.portname?.name}&delay=${it.runtime}&value=${it.status?.toInt()}&wait=${it.waittime}"
//                    var re = httpService.get(url, 12000)
                    var re = mtp.setport(it,60000)
                    var s = om.readValue<Status>(re)
                    status = "Set ${it.portname?.name} to ${it.status?.name} uptime:${s.uptime}  status:${s.status}  Pm2.5:${s.pm25} pm10:${s.pm10} pm1:${s.pm1}"
                }
            }
        } catch (e: Exception) {
            logger.error("${e.message}")
            mtp.lgs.createERROR("${e.message}", Date(), "DustWorker", "",
                    "", "run", mac, pijob.refid)
            status = "ERROR ${e.message}"
            isRun = false
        }

        exitdate = findExitdate(pijob,(totalrun+totalwait).toLong())
        status = "End job"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DustWorker::class.java)
    }
}