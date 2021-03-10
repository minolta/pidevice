package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.DistanceService
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.ReadDistanceWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("!test")
class Readdistancejob(val findJob: FindJob, val mactoipService: MactoipService,
                      val ds:DistanceService, val task: TaskService, val lgs: LogService,
                      val pideviceService: PideviceService) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var mac:String?=null
        var refid:Long? = 0L
        try {
            var jobs = findJob.loadjob("readdistance")

            if(jobs!=null)
            {
                jobs.forEach {
                    mac = it.desdevice?.mac
                    refid = it.refid
                    if(!task.checkrun(it))
                    {
                        var t = ReadDistanceWorker(it,mactoipService,ds,pideviceService)
                        task.run(t)
                    }
                }
            }


        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),"Readdistancejob","run"
            ,"23","run",mac,refid)
            logger.error(e.message)
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Readdistancejob::class.java)
    }
}