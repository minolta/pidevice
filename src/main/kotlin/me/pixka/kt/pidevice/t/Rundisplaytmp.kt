package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.DisplaytmpWorker
import me.pixka.log.d.LogService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class Rundisplaytmp(
    val lgs: LogService, val mtp: MactoipService, val findJob: FindJob,
    val task: TaskService, val ct: CheckTimeService
) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var mac: String? = null
        var jobid: Long? = null
        var ownid: Long? = null
        try {
            var jobs = findJob.loadjob("displaytmp")

            if (jobs != null) {
                jobs.forEach {
                    mac = it.desdevice?.mac
                    jobid = it.refid
                    ownid = it.pidevice_id
                    if (!task.checkrun(it)) {
                        task.run(DisplaytmpWorker(it, mtp))
                    }
                }
            }

        } catch (e: Exception) {
            lgs.createERROR(
                "${e.message}", Date(), "Rundisplaytmp",
                "", "18", "run", mac, jobid, ownid
            )
        }

    }

}