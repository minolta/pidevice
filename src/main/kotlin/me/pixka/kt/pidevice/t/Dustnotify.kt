package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.FindJob
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * สำหรับหา job ที่ ดูเรื่องของฝุ่น
 */
@Component
class Dustnotify(val findJob: FindJob) {

    @Scheduled(fixedDelay = 2000)
    fun runDustcheck() {
        var jobs = findJob.loadjob("checkdust")

        if(jobs!=null)
        {
            jobs.forEach {
                
            }
        }


    }
}