package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.DustcheckWorker
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * สำหรับหา job ที่ ดูเรื่องของฝุ่น
 */
@Component
@Profile("!test")
class Dustnotify(val findJob: FindJob,val task:TaskService,val mtp:MactoipService,val ntf:NotifyService) {

    @Scheduled(fixedDelay = 2000)
    fun runDustcheck() {
        var jobs = findJob.loadjob("checkdust")
        if(jobs!=null)
        {
            jobs.forEach {
                if(!task.checkrun(it))
                {
                    task.run(DustcheckWorker(it,mtp,ntf))
                }
            }
        }


    }
}