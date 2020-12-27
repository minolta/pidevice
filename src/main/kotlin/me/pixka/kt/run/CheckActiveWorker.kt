package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.CompletableFuture

class CheckActiveWorker(job: Pijob, val mtp: MactoipService, val ntfs: NotifyService)
    : DWK(job), Runnable {


    var logger = LoggerFactory.getLogger(CheckActiveWorker::class.java)
    override fun run() {
        isRun = true
        startRun = Date()
        status = "Start run ${startRun}"
        logger.debug(" checkactive Start run ${startRun} ${pijob.name}")
        var token = pijob.token
        var mac: String? = null

        try {
            var ports = mtp.getPortstatus(pijob) as List<Portstatusinjob>

            ports = ports.filter { it.enable == true }

            logger.debug("Found port ${ports.size} ${pijob.name}")

            for (port in ports) {

                if (port.enable != null && port.enable!!) {

                    CompletableFuture.supplyAsync {
                        var name = port.device?.name
                        var mac = port.device?.mac

                        try {
                            var ip = mtp.mactoip(mac!!)
                            status = "Check ${name} at  ${ip}"
                            var re = mtp.http.get("http://${ip}", 5000)
                            var s = mtp.om.readValue<Status>(re)
                            status = "${name} is Active uptime ${s.uptime}"

                        }catch (e: SocketTimeoutException) {
                            if(token!=null)
                            {
                                ntfs.message("Check active ${name} Timeout ${e.message}",token)
                            }
                            else
                                ntfs.message("Check active ${name} Timeout ${e.message}")

                            logger.error(e.message)
                            throw e

                        }
                        catch (e: Exception) {
                            mtp.lgs.createERROR("ERROR ${e.message}",Date(),
                                    "CheckActiveWorker",Thread.currentThread().name,"39")
                            logger.error(e.message)
                            if(token!=null)
                            {
                                ntfs.message("${name} ERROR: ${e.message}",token)
                            }
                            else
                                ntfs.message("${name} ERROR: ${e.message}")

                            throw e

                        }



                    }

                }

            }
            status = "Sumit all Task"
            exitdate = findExitdate(pijob)


        } catch (e: Exception) {
            logger.error(e.message)
            mtp.lgs.createERROR("Error ${e.message} ${pijob.name}", Date(), "",
                    "", "", "", "${mac}", pijob.refid)
            status = "Error ${e.message} ${pijob.name}"
        }

        exitdate = findExitdate(pijob)
        status = "End run wait exit date"
    }

    override fun toString(): String {
        return "CHECKACTIVE NAME ${pijob.name} ${startRun} ${status}"
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
