package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.PressureWorker
import me.pixka.kt.run.PressureOverWorker
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 *
 * สำหรับจับเวลาแล้วทำงานตามที่กำหนด
 */
@Component
class RunPressureOveruse(val findJob: FindJob, val task: TaskService, val mac: MactoipService, val ntfs: NotifyService) {

    @Scheduled(fixedDelay = 2000)
    fun run()
    {
        var jobs = findJob.loadjob("perssureoverjob")

        if(jobs!=null)
        {
            jobs.forEach {
                if(!task.checkrun(it))
                {
                    var p = PressureOverWorker(it,mac,ntfs)
                    task.run(p)
                }

            }
        }
    }

}