package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.MactoipService
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
class ReadTmp(val pjs: PijobService, val js: JobService, val ts: TaskService,
              val dvs: Ds18valueService,
              val pideviceService: PideviceService, val findJob: FindJob,
              val taskService: TaskService, val mtp: MactoipService) {
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
                        var t= mtp.readTmp(i)?.toDouble()
                        if (i.tlow?.toDouble()!! <= t!!
                                && taskService.runinglist.find {
                                    it.getPijobid() == i.id
                                            && it.runStatus()
                                } == null) {

                            if (!taskService.checkrun(i)) {
                                var t = ReadTmpTask(i, null, pideviceService, dvs, mtp)
                                taskService.run(t)
                            }
                        }
                    } else {

                        if (!taskService.checkrun(i)) {
                            var t = ReadTmpTask(i, null, pideviceService, dvs, mtp)
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


         var logger = LoggerFactory.getLogger(ReadTmp::class.java)
}