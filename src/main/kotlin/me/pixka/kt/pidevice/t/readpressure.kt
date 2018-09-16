package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PideviceService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Future

@Component
@Profile("pi")
class Findreadpressure(val pideviceService: PideviceService, val ps: PressurevalueService,
                       val js: JobService, val pjs: PijobService,
                       val http: HttpControl, val ips: IptableServicekt, val ts: TaskService,
                       val rp:readP) {

    val om = ObjectMapper()
    @Scheduled(initialDelay = 10000, fixedDelay = 5000)
    fun find() {
        logger.info("Start read Pressure")
        var jobtorun = loadJob()
        try {
            if (jobtorun != null) {
                logger.debug("Found job  ${jobtorun.size}")
                for (j in jobtorun) {


                    //var value = read(j)

                    var f = readAsyn(j)
                    logger.debug("Read pi job task ${f}")
                    var value: PressureValue? = null

                    if (f != null)
                        value = ts.runAsyn(f, 5) as PressureValue
                    if (value != null) {
                        logger.debug("Save Pressure")
                        try {
                            value.device = pideviceService.findByMac(value.device?.mac!!)
                            ps.save(value)
                        } catch (e: Exception) {
                            logger.error("Save Error: ${e.message}")
                        }
                    } else
                        logger.error("Pressure is null ${value}")


                }
            }
        } catch (e: Exception) {
            logger.error("Find read pressure ${e.message}")
            throw e
        }

        logger.debug("End Task read pressure")
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


    fun readAsyn(j: Pijob): Future<Any>? {
        logger.debug("Start read asyn ID:${j.id}")
        var f =  rp.readAsyn(j)
        logger.debug("Return  ID:${j.id} ==> ${f} ")
        return f

    }

    //ใช้สำหรับอ่าน node mcu ค่าแรงดัน

    fun read(j: Pijob): PressureValue? {
        var des = j.desdevice
        if (des == null) {
            logger.error("Device not found ${des}")
            return null
        }

        var url = "/pressure"

        var ip = ips.findByMac(des.mac!!)
        if (ip != null) {
            var ipstring = ip.ip
            var re = ""
            try {

                var u = "http://${ipstring}${url}"
                logger.debug("Read pressure ${u}")
                re = http.get(u)
            } catch (e: Exception) {
                logger.error(e.message)
                throw e
            }
            try {
                logger.debug("Pase value")
                var ps = om.readValue<PressureValue>(re, PressureValue::class.java)
                logger.debug("Pressure value ${ps}")
                return ps
            } catch (e: Exception) {
                logger.error(e.message)
                throw  e

            }

        }
        logger.error("Can not find ip")
        throw Exception("Can not find ip")


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Findreadpressure::class.java)
    }

}


@Component
class readP(val ips: IptableServicekt,val http:HttpControl)
{
    val om = ObjectMapper()
    @Async("aa")
    fun readAsyn(j: Pijob): Future<Any>? {
        logger.debug("Start read asyn")
        try {
            var des = j.desdevice
            if (des == null) {
                logger.error("Device not found ${des}")
                return null
            }

            var url = "/pressure"

            var ip = ips.findByMac(des.mac!!)
            if (ip != null) {
                var ipstring = ip.ip
                var re = ""
                try {

                    var u = "http://${ipstring}${url}"
                    logger.debug("Read pressure ${u}")
                    re = http.get(u)
                    logger.debug("Return ${re}")
                } catch (e: Exception) {
                    logger.error(e.message)
                    throw e
                }
                try {
                    logger.debug("Pase value")
                    var ps = om.readValue<PressureValue>(re, PressureValue::class.java)
                    logger.debug("Pressure value ${ps}")
                    return AsyncResult(ps)
                } catch (e: Exception) {
                    logger.error(e.message)
                    throw  e

                }

            }
            logger.error("Can not find ip")
            throw Exception("Can not find ip")

        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }
    companion object {
        internal var logger = LoggerFactory.getLogger(readP::class.java)
    }

}