package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.PressureWorker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RunperssureJob(val findJob: FindJob,val task:TaskService,val mac:MactoipService) {

    @Scheduled(fixedDelay = 2000)
    fun run()
    {
        var jobs = findJob.loadjob("perssurejob")

        if(jobs!=null)
        {
            jobs.forEach {


                if(!task.checkrun(it))
                {
                    var p = PressureWorker(it,mac)
                    task.run(p)
                }

            }
        }
    }

}