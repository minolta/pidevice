package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.PijobrunInterface
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class DisplaytmpWorker(var pijob: Pijob,
                       val lgs: LogService,
                       val httpService: HttpService,
                       val iptableServicekt: IptableServicekt,
                       val ports: List<Portstatusinjob>?,val ct:CheckTimeService) : PijobrunInterface, Runnable {
    var isRun = true
    var status = ""
    var sd: Date? = null
    var exitdate: Date? = null
    val om = ObjectMapper()
    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
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
        return sd
    }

    override fun state(): String? {
        return status
    }

    override fun setrun(p: Boolean) {
        isRun = p
    }

    override fun exitdate(): Date? {
        return exitdate
    }

    override fun run() {
        sd = Date()
        isRun = true
        status = "Start run"
        var mac: String? = null
        var refid: Long? = 0L

        try {
            val t = readTemp(pijob)
            var name = pijob.desdevice?.name
            if (t != null && ports != null) {

                ports.filter { it.enable == true }.forEach {
                    val displayip = ip(it.device?.mac!!)
                    mac = it.device?.mac
                    refid = it.refid
//                    val name = it.device?.name
                    if (displayip != null) {
                        var url = httpService.encode("ความร้อน : ${name} = ${t}")
                        var u = "http://${displayip}/settext?t=${url}&tn=2"
                        status = u
                        var re = httpService.get(u, 10000)
                        status = "set Tmp ${name} ${t} ${re}"
                        exitdate = ct.findExitdate(pijob)
                    }
                    else
                    {
                        lgs.createERROR("display ip no found",Date(),"DisplaytmpWorker",
                        "","79","run()",it.device?.mac)
                        logger.error("Display ip not found")
                        status ="Display ip not found"
                        isRun=false
                    }
                }


            } else {
                logger.error("IP ERROR")
                isRun=false
            }
        } catch (e: Exception) {
            logger.error("Run() ${e.message}")
            status = "${e.message}"
            lgs.createERROR("${e.message}", Date(),
                    "DisplaytmpWork", "", "63", "run()", mac, refid)
            isRun = false
        }


        println("StartDATE: ${sd} exitdate:${exitdate}")

//        status = "Job end wait for exit ${exitdate}"
        //ถ้าทุกอย่างปกติกให้ taskserver เป็น คน end เอง
    }



    fun readTemp(job: Pijob): Double? {
        try {
            val readip = ip(job.desdevice?.mac!!)
            var re = httpService.get("http://${readip}", 5000)
            var tmp = om.readValue<Tmpobj>(re)
            var tv = 0.0
            if (tmp.t != null) {
                tv = tmp.t?.toDouble()!!
            }

            if (tmp.tmp != null)
                tv = tmp.tmp?.toDouble()!!

            return tv

        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "Rundisplaytmp", "", "43", "readTemp()")
            logger.error(" Read tmp ${e.message}")
        }
        return null
    }

    fun ip(mac: String): String? {
        try {
            val ip = iptableServicekt.findByMac(mac)
            if (ip != null) {
                return ip.ip
            }
            else
            {
                lgs.createERROR("Ip not found ${mac}",Date(),"DisplaytmpWorker",
                "","135","ip()",mac)
            }
            return null
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "Rundisplaytmp", "", "43", "ip()", mac)
            logger.error("${e.message}")
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DisplaytmpWorker::class.java)
    }

}