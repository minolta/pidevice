package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.ReadPressureTask
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
//@Profile("pi")
class Findreadpressure(val pideviceService: PideviceService, val ps: PressurevalueService,
                       val js: JobService, val pjs: PijobService,
                       val http: HttpControl, val ips: IptableServicekt, val ts: TaskService, val ntf: NotifyService
) {


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun find() {

        // testread()
        logger.info("Start read Pressure")
        var jobtorun = loadJob()
        try {
            if (jobtorun != null) {
                logger.debug("Found job  ${jobtorun.size}")
                for (j in jobtorun) {

                    var t = ReadPressureTask(j,null,ips,ps,pideviceService,ntf)
                    ts.run(t)
                }
            }
        } catch (e: Exception) {
            logger.error("4 ${e.message}")
            //throw e
        }

        logger.debug(" 5 End Task read pressure")
    }

    fun loadJob(): List<Pijob>? {
        try {
            var job = js.findByName("readpressure")

            if (job != null) {
                var jobtorun = pjs.findJob(job.id)
                return jobtorun
            }
        } catch (e: Exception) {
            logger.error("Loadjob: ${e.message}")
            throw e
        }

        return null

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Findreadpressure::class.java)
    }

}
//
//
//class readP(val ips: IptableServicekt, val http: HttpControl) : Callable<PressureValue> {
//    private var j: Pijob? = null
//
//    fun setJob(j: Pijob) {
//        this.j = j
//    }
//
//    override fun call(): PressureValue? {
//        logger.debug("Start read asyn")
//        try {
//            var des = j?.desdevice
//            if (des == null) {
//                logger.error("Device not found ${des}")
//                return null
//            }
//
//            var url = "/pressure"
//
//            var ip = ips.findByMac(des.mac!!)
//            if (ip != null) {
//                var ipstring = ip.ip
//                var re: String? = ""
//                var executor = Executors.newSingleThreadExecutor()
//                try {
//
//                    var u = "http://${ipstring}${url}"
//                    var get = HttpGetTask(u)
//                    logger.debug("Read pressure ${u}")
//                    //re = http.get(u)
//                    var f = executor.submit(get)
//                    re = f.get(2, TimeUnit.SECONDS)
//                    logger.debug("Return ${re}")
//                    if (re != null) {
//                        try {
//                            var o = om.readValue<PressureValue>(re)
//                            return o
//                        } catch (e: Exception) {
//                            logger.error("Error pras value ${e.message}")
//                        }
//                    }
//                } catch (e: Exception) {
//                    logger.error("Get value ${e.message}")
//                    executor.shutdownNow()
//
//                    return null
//                }
//                try {
//                    logger.debug("Pase value")
//                    var ps = om.readValue<PressureValue>(re, PressureValue::class.java)
//                    logger.debug("Pressure value ${ps}")
//                    return ps
//                } catch (e: Exception) {
//                    logger.error("Top error ${e.message}")
//                    return null
//
//                }
//
//            }
//            logger.error("Can not find ip")
//            return null
//
//        } catch (e: Exception) {
//            logger.error("global ${e.message}")
//            return null
//        }
//
//
//    }
//
//    val om = ObjectMapper()
//    fun readAsyn(j: Pijob): Future<Any>? {
//        logger.debug("Start read asyn")
//        try {
//            var des = j.desdevice
//            if (des == null) {
//                logger.error("Device not found ${des}")
//                return CompletableFuture.completedFuture(null)
//            }
//
//            var url = "/pressure"
//
//            var ip = ips.findByMac(des.mac!!)
//            if (ip != null) {
//                var ipstring = ip.ip
//                var re = ""
//                try {
//
//                    var u = "http://${ipstring}${url}"
//                    logger.debug("Read pressure ${u}")
//                    re = http.get(u)
//                    logger.debug("Return ${re}")
//                } catch (e: Exception) {
//                    logger.error(e.message)
//                    return CompletableFuture.completedFuture(null)
//                }
//                try {
//                    logger.debug("Pase value")
//                    var ps = om.readValue<PressureValue>(re, PressureValue::class.java)
//                    logger.debug("Pressure value ${ps}")
//                    return CompletableFuture.completedFuture(ps)
//                } catch (e: Exception) {
//                    logger.error(e.message)
//                    return CompletableFuture.completedFuture(null)
//
//                }
//
//            }
//            logger.error("Can not find ip")
//            return CompletableFuture.completedFuture(null)
//
//        } catch (e: Exception) {
//            logger.error(e.message)
//            return CompletableFuture.completedFuture(null)
//        }
//    }
//
//    companion object {
//        internal var logger = LoggerFactory.getLogger(readP::class.java)
//    }
//
//}