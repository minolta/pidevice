package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PideviceService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class Findreadpressure(val pideviceService: PideviceService,val ps: PressurevalueService, val js: JobService, val pjs: PijobService, val http: HttpControl, val ips: IptableServicekt) {

    val om = ObjectMapper()
    @Scheduled(initialDelay = 10000, fixedDelay = 15000)
    fun find() {
        logger.info("Start read Pressure")

        var job = js.findByName("readpressure")
        if (job != null) {
            var jobtorun = pjs.findJob(job.id)

            if (jobtorun != null) {
                logger.debug("Found job  ${jobtorun.size}")
                for (j in jobtorun) {
                    var value = read(j)
                    if (value != null) {
                        logger.debug("Save Pressure")
                    try {
                        value.device = pideviceService.findByMac(value.device?.mac!!)
                        ps.save(value)
                    }catch (e:Exception)
                    {
                        logger.error("Save Error: ${e.message}")
                    }
                    } else
                        logger.error("Pressure is null ${value}")
                }
            }
        }


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