package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.Dusttotm1Worker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * สำหรับแสดงผลไป tm1 ของ esp32 sensor
 */
@Component
class RunDisplaydusttoTm1(val findJob: FindJob, val task: TaskService, val mtc: MactoipService) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var jobs = findJob.loadjob("dusttotm1")
            if (jobs != null) {
                jobs.forEach {
                    if (!task.checkrun(it)) {
                        task.run(Dusttotm1Worker(it, mtc))
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Run dust to tm1 error ${e.message}")
        }
    }

    var logger = LoggerFactory.getLogger(RunDisplaydusttoTm1::class.java)
}