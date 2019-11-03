package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18sensor
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.ReadTmpTask
import me.pixka.pibase.s.*
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
              val pideviceService: PideviceService,
              val ips: IptableServicekt, val taskService: TaskService, val readUtil: ReadUtil) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        try {
            logger.debug("Run #readtemp")
            var jobs = loadjob()
            logger.debug("Found job ${jobs}")

            if (jobs != null)
                for (i in jobs) {

                    logger.debug("Run job ${i}")
                    if (i.ds18sensor != null) {
                        var t = ReadTmpTask(i, readUtil, ips, dvs, pideviceService, dss)
                        taskService.run(t)
                    } else {
                        var t = ReadTmpTask(i, readUtil, ips, dvs, pideviceService, dss)
                        taskService.run(t)

                    }

                }
        } catch (e: Exception) {
            logger.error("Read OTher ds ERROR ${e.message}")
        }
        logger.debug("End #readtemp")
    }

    fun readOther(desid: Long, sensor: Long?): DS18value? {
        var r = ss.readDsOther(desid, sensor)
        return r
    }

    fun checkLocal(ds: DS18sensor): DS18value? {
        try {
            var value = io.readDs18value(ds.name!!)
            return value
        } catch (e: Exception) {
            logger.error("Read local ==> Error ${e.message}")
        }
        return null
    }


    fun loadjob(): List<Pijob>? {
        var job = js.findByName("readtemp")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }


        throw Exception("Not have JOB")
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(ReadTmp::class.java)
    }
}