package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.VbattService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.D1readvoltWorker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class ReadVtask(
    val pjs: PijobService,
    val js: JobService,
    val task: TaskService,
    val mtp: MactoipService,
    val pss: VbattService, val ntf: NotifyService, val findJob: FindJob,
    val httpService: HttpService
) {
    var exitdate: Date? = null
    var om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var list = findJob.loadjob("runreadvolt")
            if (list != null)
                logger.debug("Job for ReadVtask ${list.size}")

            if (list != null) {
                list.forEach {
                    if (!task.checkrun(it)) {
                        var run = task.run(D1readvoltWorker(it, pss, mtp))
                        logger.debug("Run ? ${run}")
                    }
                }

            }
        } catch (e: Exception) {
            logger.error("ERROR READ V ${e.message}")
        }
    }

    var logger = LoggerFactory.getLogger(ReadVtask::class.java)
}