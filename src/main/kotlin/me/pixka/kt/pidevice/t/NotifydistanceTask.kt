package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.NotifyDistanceWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
//@Profile("!test")
class NotifydistanceTask(
    val findjob: FindJob, val mactoipService: MactoipService, val notifyService: NotifyService,
    val task: TaskService
) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var jobs = findjob.loadjob("notifydistance")
            if (jobs != null) {
                jobs.map {
                    try {
                        if (!task.checkrun(it)) {
                            if (it.desdevice != null) {
                                var ip = it.desdevice?.ip
                                if (ip != null) {
                                    var t = "http://${ip}"
                                    var n = NotifyDistanceWorker(it, mactoipService, t, notifyService)

                                    task.run(n)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(NotifydistanceTask::class.java)
    }
}