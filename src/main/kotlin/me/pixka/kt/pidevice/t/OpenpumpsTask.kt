package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.OpenpumpsWorker
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class OpenpumpsTask(
    val findJob: FindJob, val mtp: MactoipService,val ntfs:NotifyService,
    val task: TaskService,val timeService: CheckTimeService
) {


    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var jobs = findJob.loadjob("runopenpumps")
        if (jobs != null) {
            jobs.forEach {
                if(timeService.checkTime(it, Date())) {
                    if (!task.checkrun(it)) {
                       var t = OpenpumpsWorker(it,mtp,ntfs)
                       task.run(t)
                    }
                }
            }

        }
    }
}