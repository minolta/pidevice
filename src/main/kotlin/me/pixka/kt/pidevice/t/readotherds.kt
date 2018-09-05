package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18sensor
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.Ds18valueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * ใช้สำหรับ อ่านค่า temp ทุกอย่าง
 * Job readtmp
 * เมื่อเจอ job ที่ เป็น readtmp ระบบจะทำการตรวจสอบว่า เป็น local หรือเปล่า ถ้า เป็นก็อ่านจาก local แล้วบันทึกไว้ใน เครื่อง
 *
 */
@Component
@Profile("pi", "lite")
class ReadTmp(val pjs: PijobService, val js: JobService, val ts: TaskService, val io: Piio,
              val dvs: Ds18valueService, val ss: SensorService, val dss: DS18sensorService) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Run #readtemp")
        var jobs = loadjob()
        logger.debug("Found job ${jobs}")

        if (jobs != null)
            for (i in jobs) {

                logger.debug("Run job ${i}")
                var value = checkLocal(i.ds18sensor!!)

                if (value != null) {
                    logger.debug("Local value is ${value}")
                    dvs.save(value)

                } else {//ถ้าไม่เจอ local Sensor ต้องอ่านจากตัวอื่น
                    try {
                        value = ss.readDsOther(i.desdevice_id!!, i.ds18sensor_id!!)
                        logger.debug("Read othre value ${value} ")
                        if (value != null) {
                            logger.debug("Value is ${value.t} PI:${value.pidevice} SENSOR:${value.ds18sensor} ")

                            var d = i.ds18sensor
                            if (d != null) {
                                d = dss.findorcreate(d.name!!)
                            }
                            else
                            {
                                logger.debug("Ds18Sensor not found")
                            }
                            value.ds18sensor = d
                            value.pidevice = i.desdevice
                            dvs.save(value)
                        }
                    } catch (e: Exception) {
                        logger.error(e.message)
                    }


                }
            }

        logger.debug("End #readtemp")
    }


    fun checkLocal(ds: DS18sensor): DS18value? {
        var value = io.readDs18value(ds.name!!)
        return value
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