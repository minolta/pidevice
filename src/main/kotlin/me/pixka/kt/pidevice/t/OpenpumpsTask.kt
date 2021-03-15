package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class OpenpumpsTask(
    val findJob: FindJob, val mtp: MactoipService,
    val task: TaskService
) {


    val om = ObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var jobs = findJob.loadjob("runopenpumps")
        if (jobs != null) {
            jobs.forEach {

                if(!task.checkrun(it))
                {

                }
            }

        }
    }
}