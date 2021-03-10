package me.pixka.kt.pidevice.t
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.CheckActiveWorker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
@Profile("!test")
class Checkactive(val js: JobService, val task: TaskService,
                  val findJob: FindJob,val ntfs: NotifyService, val mtp: MactoipService) {
    var logger = LoggerFactory.getLogger(Checkactive::class.java)

    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("checkactive")
        try {
            var jobs = findJob.loadjob("checkactive")
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
            mtp.lgs.createERROR("${e.message}", Date(), "CheckActive", "",
                    "", "", System.getProperty("mac"))
            logger.error("Run checkactive error ${e.message}")
        }
    }
}