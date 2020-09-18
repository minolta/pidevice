package me.pixka.kt.pidevice.t


import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1portjobWorker
import me.pixka.kt.run.DPortstatus
import me.pixka.kt.run.GroupRunService
import me.pixka.kt.run.PorttoCheck
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture

@Component
class RunportByd1(val pjs: PijobService, val findJob: FindJob,
                  val js: JobService,
                  val task: TaskService, val ips: IptableServicekt,
                  val dhs: Dhtutil, val httpControl: HttpControl, val httpService: HttpService,
                  val dhtvalueService: DhtvalueService, val groups: GroupRunService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        logger.debug("Start get task Runportbyd1 ${Date()}")
        try {
            var list = findJob.loadjob("runportbyd1")
            if (list != null)
                logger.debug("Job for Runportbyd1 Port jobsize  ${list.size}")
            if (list != null) {
                list.forEach {
//                    Checkrun(it)
                    try {
                        var checks = getPorttocheck(it)
                        var sensorstatus = getSensorstatus(it)
                        var r = false
                        if (checks != null) {
                            for (c in checks) {
                                r = r || getsensorstatusvalue(c.name!!, c.check!!, sensorstatus!!)
                            }
                            logger.debug("R is ${r}")
                        }
                        if (r) {
                            var t = D1portjobWorker(it, pjs, httpService, task, ips)
                            var run = task.run(t)
                            logger.debug("Can run ${run}")
                        }
                    }catch (e:Exception)
                    {
                        logger.error("ERROR Set port ${it.name}  ERROR ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Read h by d1 ERROR ${e.message}")
        }
    }

    fun getPorttocheck(p: Pijob): ArrayList<PorttoCheck>? {
        try {
            var bufs = ArrayList<PorttoCheck>()
            var c = p.description?.split(",")
            D1portjobWorker.logger.debug("C: ${c}")
            if (c.isNullOrEmpty()) {
                return null
            }

            var c1 = PorttoCheck()
            var ii = 1

            c.map {
                D1portjobWorker.logger.debug("Value : ${it}")
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
            D1portjobWorker.logger.debug("ERROR ${e.message}")
        }
        return null
    }

    fun getSensorstatus(p: Pijob): DPortstatus? {
        var url = ""
        var ip:Iptableskt?=null
        try {
            logger.debug("Check Port start")
            var ip = ips.findByMac(p.desdevice?.mac!!)
            url = "http://${ip?.ip}"
            var re: String? = httpService.get(url)
            var dp = om.readValue(re, DPortstatus::class.java)
            return dp

        } catch (e: Exception) {
            logger.error("Get Sensor status JOB NAME IP:${ip} URL:${url} ${p.name} ${e.message} Des name:${p.desdevice}  ")
            throw e
        }
        return null
    }

    fun Checkrun(job: Pijob) {
        CompletableFuture.supplyAsync { // ตรวจสอบก่อนว่า Run ได้เปล่า
            var checks = getPorttocheck(job)
            var sensorstatus = getSensorstatus(job)
            var r = false
            if (checks != null) {
                for (c in checks) {
                    r = r || getsensorstatusvalue(c.name!!, c.check!!, sensorstatus!!)
                }
                logger.debug("R is ${r}")
            }
            r
        }.thenApply {
            if (it) {
                var t = D1portjobWorker(job, pjs, httpService, task, ips)
                var run = task.run(t)
                logger.debug("Can run ${run}")
            }
        }.exceptionally {
            logger.error(it.message)
        }
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
            logger.error("ERROR in getsensorstatusvalue message: ${e.message}")
        }


        return false
    }
//    var df = SimpleDateFormat("HH:mm")
//    fun checktime(job: Pijob): Boolean {
//        try {
////            df.timeZone = TimeZone.getTimeZone("+0700")
//            var n = df.format(Date())
//
//            var now = df.parse(n)
//            logger.debug("checktime N:${n} now ${now} now time ${now.time}")
//            logger.debug("checktime s: ${job.stimes} ${now} e:${job.etimes}")
//            if (job.stimes != null && job.etimes != null) {
//                var st = df.parse(job.stimes).time
//                var et = df.parse(job.etimes).time
//                logger.debug("checktime ${st} <= ${now} <= ${et}")
//                if (st <= now.time && now.time <= et)
//                    return true
//            } else if (job.stimes != null && job.etimes == null) {
//                var st = df.parse(job.stimes).time
//                logger.debug("checktime ${st} <= ${now} ")
//                if (st <= now.time)
//                    return true
//            } else if (job.stimes == null && job.etimes != null) {
//                var st = df.parse(job.etimes).time
//                logger.debug("checktime ${st} >= ${now}")
//                if (st <= now.time)
//                    return true
//            } else {
//                logger.debug("${job.name} checktime not set ")
//                return true
//            }
//        } catch (e: Exception) {
//            logger.error("checktime ${e.message}")
//        }
//
//        return false
//    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runportbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunportByd1::class.java)
    }
}