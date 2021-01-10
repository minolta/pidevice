package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.DisplaydhtWorker
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DisplayDHT(val findJob: FindJob,val task:TaskService,val mtp: MactoipService,
                 val portstatusinjobService: PortstatusinjobService) {


    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var jobs = findJob.loadjob("displaydht")

            if(jobs!=null)
            {
                jobs.forEach {

                    if(!task.checkrun(it))
                    {
                        var t = DisplaydhtWorker(it,mtp)
                        task.run(t)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR DISPLAY DHT ${e.message}")
        }
    }

    var logger = LoggerFactory.getLogger(DisplayDHT::class.java)

}