package me.pixka.kt.pidevice.t


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1portjobWorker
import me.pixka.kt.run.DPortstatus
import me.pixka.kt.run.PorttoCheck
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class RunportByd1(val pjs: PijobService, val findJob: FindJob,
                  val js: JobService,
                  val task: TaskService, val ips: IptableServicekt,
                  val dhs: Dhtutil,val mtp:MactoipService,val ntfs:NotifyService,
                  val httpService: HttpService, val lgs: LogService,val checkTimeService: CheckTimeService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        logger.debug("Start get task Runportbyd1 ${Date()}")
        var mac: String? = null
        try {
            var list = findJob.loadjob("runportbyd1")
            if (list != null)
                logger.debug("Job for Runportbyd1 Port jobsize  ${list.size}")
            if (list != null) {
                list.forEach {
                    try {
                        if(checkTimeService.checkTime(it,Date())) {
                            var checks = getPorttocheck(it)
                            var sensorstatus = getSensorstatus(it)
                            mac = it.desdevice?.mac
                            var r = false
                            if (checks != null) {
                                for (c in checks) {
                                    r = r || getsensorstatusvalue(c.name!!, c.check!!, sensorstatus!!)
                                }
                                logger.debug("R is ${r}")
                            }
                            if (r && !task.checkrun(it)) {

                                var t = D1portjobWorker(it, mtp,ntfs)
                                var run = task.run(t)
                                logger.debug("Can run ${run}")
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("ERROR Set port ${it.name}  ERROR ${e.message}")
                        lgs.createERROR("${e.message}", Date(),
                                "RunportByd1", "", "41", "run", mac, it.refid)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Read h by d1 ERROR ${e.message}")
            lgs.createERROR("${e.message}", Date(),
                    "RunportByd1", "", "35", "run", mac)
        }
    }

    fun getPorttocheck(p: Pijob): ArrayList<PorttoCheck>? {
        try {
            var bufs = ArrayList<PorttoCheck>()
            var c = p.description?.split(",")
            logger.debug("C: ${c}")
            if (c.isNullOrEmpty()) {
                return null
            }

            var c1 = PorttoCheck()
            var ii = 1

            c.map {
                logger.debug("Value : ${it}")
                if (it.toIntOrNull() == null)
                    c1.name = it
                else {
                    c1.check = it.toInt()
                    bufs.add(c1)
                    c1 = PorttoCheck()
                }
            }

            return bufs


        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(), "RunportByd1",
                    "", "73", "getPorttocheck",
                    p.desdevice?.mac, p.refid)
            logger.debug("ERROR ${e.message}")
        }
        return null
    }

    fun getSensorstatus(p: Pijob): DPortstatus? {
        var url = ""
        val ip: Iptableskt? = null
        try {
            logger.debug("Check Port start")
            val ip = ips.findByMac(p.desdevice?.mac!!)
            url = "http://${ip?.ip}"
            val re: String? = httpService.get(url,20000)
            val dp = om.readValue<DPortstatus>(re!!)
            return dp

        } catch (e: Exception) {
            logger.error("Get Sensor status JOB NAME IP:${ip} URL:${url} ${p.name} ${e.message} Des name:${p.desdevice}  ")
            lgs.createERROR("${e.message}",
                    Date(), "RunportByd1", "",
                    "110", "getSensorstatus", p.desdevice?.mac, p.refid)

        }
        return null
    }


    fun getsensorstatusvalue(n: String, c: Int, sensorstatus: DPortstatus?): Boolean {
        val nn = n.toLowerCase()
        try {
            if (nn.equals("d1")) {
                if (sensorstatus?.d1 == c)
                    return true
            } else if (nn.equals("d2")) {
                if (sensorstatus?.d2 == c)
                    return true
            } else if (nn.equals("d3")) {
                if (sensorstatus?.d3 == c)
                    return true
            } else if (nn.equals("d4")) {
                if (sensorstatus?.d4 == c)
                    return true
            } else if (nn.equals("d5")) {
                if (sensorstatus?.d5 == c)
                    return true
            } else if (nn.equals("d6")) {
                if (sensorstatus?.d6 == c)
                    return true
            } else if (nn.equals("d7")) {
                if (sensorstatus?.d7 == c)
                    return true
            } else if (nn.equals("d8")) {
                if (sensorstatus?.d8 == c)
                    return true
            }
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "RunportByd1", "", "130",
                    "getsensorstatusvalue", sensorstatus?.mac)
            logger.error("ERROR in getsensorstatusvalue message: ${e.message}")
        }


        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunportByd1::class.java)
    }
}