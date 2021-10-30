package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.CheckActiveWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class Cooldownjob(val js: JobService, val task: TaskService,
                  val findJob: FindJob, val ntfs: NotifyService, val mtp: MactoipService
) {

    var logger = LoggerFactory.getLogger(Cooldownjob::class.java)

    @Scheduled(fixedDelay = 1000)
    fun run() {
        logger.debug("Cooldownjob")
        try {
            var jobs = findJob.loadjob("cooldownjob")
            if (jobs != null) {
                logger.debug("Found checkactive ${jobs.size} ")

                jobs.forEach {
                    CompletableFuture.supplyAsync {
                        if (!task.checkrun(it)) {
                            var t = CheckActiveWorker(it, mtp, ntfs)
                            var run = task.run(t)
                        }
                    }.exceptionally {
                        logger.error(it.message)
                    }
                }
            }
        } catch (e: Exception) {
            mtp.lgs.createERROR("${e.message}", Date(), "Cooldownjob", "",
                "", "", System.getProperty("mac"))
            logger.error("Run Cooldownjob error ${e.message}")
        }
    }
}