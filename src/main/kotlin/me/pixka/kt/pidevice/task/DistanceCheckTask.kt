package me.pixka.kt.pidevice.task

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.CheckdistancecheckWorker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class DistanceCheckTask(
    val findJob: FindJob,
    val ts: TaskService,
    val mactoipService: MactoipService,
    val ntfs: NotifyService
) {
    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var jobs = findJob.loadjob("distancecheck")

            if (jobs != null) {
                jobs.forEach {

                    if (!ts.checkrun(it)) {
                        ts.run(CheckdistancecheckWorker(it, ntfs, mactoipService))
                    }

                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    var logger = LoggerFactory.getLogger(DistanceCheckTask::class.java)
}