package me.pixka.kt.run
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CheckActiveWorker(var pijob: Pijob, val ps: PortstatusinjobService,
                        val ips: IptableServicekt, val ntfs: NotifyService)
    : PijobrunInterface, Runnable {
    companion object {
        internal var logger = LoggerFactory.getLogger(CheckActiveWorker::class.java)
    }

    val om = ObjectMapper()
    var isRun = false
    var state = "Init"
    var startrun: Date? = null
    override fun setP(pijob: Pijob) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun run() {
        isRun = true
        startrun = Date()
        logger.debug("Start run ${startrun} ${pijob.name}")
        var token = pijob.description


        try {
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
            logger.debug("Found jobs ${ports.size} ${pijob.name}")
            for (port in ports) {
                if (port.enable != null && port.enable!!) {
                    val device = port.device
                    logger.debug("Find device ip ${device} mac ${device!!.mac} ${pijob.name}")
                    var ip = ips.findByMac(device!!.mac!!)
                    if (ip != null) {
                        var url = "http://${ip.ip}/status"
                        logger.debug("Call ${url} ${pijob.name}")
                        var http = HttpGetTask(url)
                        var t = Executors.newSingleThreadExecutor()
                        var f = t.submit(http)
                        try {
                            state = "startcall ${url} ${pijob.name}"
                            logger.debug("startcall ${url} ${pijob.name}")
                            var re = f.get(30, TimeUnit.SECONDS)
                            logger.debug("return from get ${re}")
                            try {
                                state = "return status ${re} ${pijob.name}"
                                var status = om.readValue<Status>(re, Status::class.java)
                                //end
                                state = "return status ${status} ${pijob.name} "

                            } catch (e: Exception) {
                                logger.error("Error  ${e.message}")
                                if (token != null)
                                    ntfs.message("device not respone ${device.name} ${pijob.name} ${e.message}", token)
                                else
                                    ntfs.message("device not respone ${device.name} ${pijob.name}  ${e.message}")
                            }

                        } catch (e: Exception) {
                            if (token == null)
                                ntfs.message("device not respone ${device.name} ${e.message}")
                            else
                                ntfs.message("device not respone ${device.name} ${e.message}", token)

                            state = "error ${e.message} ${pijob.name}"


                        }
                    } else {
                        logger.debug("Not found ip")
                        state = "Not found ip"
                    }
                }
            }


            var p = pijob.runtime
            if (p != null) {
                state = "Runtime ${p.toLong()}"
                TimeUnit.SECONDS.sleep(p.toLong())
            }

            var w = pijob.waittime
            if (w != null) {
                state = "Wait ${w.toLong()}"
                TimeUnit.SECONDS.sleep(w.toLong())
            }

        } catch (e: Exception) {
            logger.error(e.message)
            state = "Error ${e.message} ${pijob.name}"
        }

        isRun = false
    }

    override fun toString(): String {
        return "CHECKACTIVE NAME ${pijob.name} ${startrun} ${state}"
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class Status(var message: String? = null, var ip: String? = null,var uptime:Long?=0,
             var name: String? = null, var t: BigDecimal? = null, var h: BigDecimal? = null, var ssid: String? = null,
             var version: String? = null) {
    override fun toString(): String {
        return "${name} ${message} ${ssid} ${version} ${t} ${h} ${ip}"
    }
}
