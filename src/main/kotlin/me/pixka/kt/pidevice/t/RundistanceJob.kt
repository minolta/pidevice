package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.DistanceWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class RundistanceJob(
    val findJob: FindJob,
    val mtp: MactoipService, val task: TaskService,
    var ps: PortstatusinjobService
) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var jobs = findJob.loadjob("distancejob")
            if (jobs != null) {
                jobs.forEach {
                    if (!task.checkrun(it)) {
                        if (task.checktime(it)) {
                            var t = DistanceWorker(it, mtp, ps)
                            if (!task.run(t)) {
                                mtp.lgs.createERROR(
                                    "Not run job ${it.name}", Date(),
                                    "RundistanceJob", Thread.currentThread().name, "31",
                                    "run()", it.desdevice?.mac, it.refid, it.pidevice?.refid
                                )
                                logger.error("Not run job ${it.name}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            mtp.lgs.createERROR(
                "${e.message}", Date(),
                "RundistanceJob", Thread.currentThread().name, "16", "run()"
            )
            logger.error("${e.message}")
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RundistanceJob::class.java)
    }
}
