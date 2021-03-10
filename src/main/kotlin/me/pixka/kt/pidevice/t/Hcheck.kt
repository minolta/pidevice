package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.CheckHsensorWorker
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class Hcheck(val findJob: FindJob,val task:TaskService,val mtp:MactoipService,val line:NotifyService) {


    @Scheduled(fixedDelay = 2000)
    fun runCheck()
    {
        var jobs = findJob.loadjob("checkhsensor")

        if(jobs!=null)
        jobs.forEach {

            if(!task.checkrun(it))
            {
               var worker =  CheckHsensorWorker(it,mtp,line)
                task.run(worker)
            }
        }

    }

}