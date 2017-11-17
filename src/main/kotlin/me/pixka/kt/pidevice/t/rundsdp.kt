package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.DSDPWorker
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class RunDSDP(val pjs:PijobService,val js:JobService,val ts:TaskService,val ss:SensorService,val dps:DisplayService) {

    @Scheduled(initialDelay = 2000,fixedDelay = 30000)
    fun run()
    {
        logger.info("Run DSDP ")

        var job = js.findByName("DSDP") //สำหรับแสดงผล

        if(job==null)
        {
            logger.debug("Job not found DSDP")
            return
        }

        var jobs = pjs.findByDSDP(job.id)

        if(jobs!=null)
        {
            logger.debug("Found DSDP job  ${jobs.size} s")
            for(job in jobs)
            {
                var work = DSDPWorker(ss,dps, job as Pijob)
                ts.run(work)
            }
        }



    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunDSDP::class.java)
    }
}