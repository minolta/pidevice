package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.Tmpobj
import me.pixka.kt.run.ReadTmpTask
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * ใช้สำหรับ อ่านค่า temp ทุกอย่าง
 * Job readtmp
 * เมื่อเจอ job ที่ เป็น readtmp ระบบจะทำการตรวจสอบว่า เป็น local หรือเปล่า ถ้า เป็นก็อ่านจาก local แล้วบันทึกไว้ใน เครื่อง
 *
 */
@Component
//@Profile("pi", "lite")
class ReadTmp(val pjs: PijobService, val js: JobService, val ts: TaskService, val io: Piio,
              val dvs: Ds18valueService, val ss: SensorService, val dss: DS18sensorService,
              val pideviceService: PideviceService, val findJob: FindJob, val httpService: HttpService,
              val ips: IptableServicekt, val taskService: TaskService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {
        try {
            logger.debug("Run #readtemp")
            var jobs = findJob.loadjob("readtemp")
            logger.debug("Found job ${jobs}")
            if (jobs != null)
                for (i in jobs) {
                    logger.debug("Run job ${i}")
                    if (i.tlow != null) {
                        var ip = ips.findByMac(i.desdevice?.mac!!)
                        var re = httpService.get("http://${ip?.ip}",2000)
                        var o = om.readValue<Tmpobj>(re)
                        var t = o.tmp?.toDouble()
                        if (i.tlow?.toDouble()!! <= t!!
                                && taskService.runinglist.find {
                                    it.getPijobid() == i.id
                                            && it.runStatus()
                                } == null) {
                            var t = ReadTmpTask(i, null, ips, o, pideviceService, httpService, dvs)
                            taskService.run(t)
                        }
                    } else {
                        if(taskService.runinglist.find { it.getPijobid()==i.id && it.runStatus() }==null) {
                            var t = ReadTmpTask(i, null, ips, null, pideviceService, httpService, dvs)
                            var r = taskService.run(t)
                            logger.debug("Run ${r}")
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error("Read OTher ds ERROR ${e.message}")
        }
        logger.debug("End #readtemp")
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(ReadTmp::class.java)
    }
}