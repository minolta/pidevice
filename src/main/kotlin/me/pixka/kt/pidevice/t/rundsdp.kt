package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.o.DisplayTask
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.DSDPWorker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class RunDSDP(val pjs: PijobService, val js: JobService, val ts: TaskService, val ss: SensorService, val dps: DisplayService, val dt: DisplayTask) {

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    fun run() {
        logger.info(" Find job Run DSDP ")
        var job = js.findByName("DSDP") //สำหรับแสดงผล

        if (job == null) {
            logger.error("Job not found DSDP")
            return
        }


        var jobs = pjs.findByDSDP(job.id)

        if (jobs != null) {
            logger.debug("Found DSDP job  ${jobs.size} s And use DT")
            for (job in jobs) {
                //var work = DSDPWorker(ss, dps, job as Pijob)
                try {
                    var dpdst = DSDPWorker(ss, dps, job as Pijob)
                    //dpdst.pijob = job as Pijob
                    if (ts.checkalreadyrun(dpdst) != null) {
                        logger.debug("Can run DSDP job")
                        //var f = dpdst.runasync()
                        dt.d = dpdst
                        var f = dt.run()
                        if (f != null) {
                            var i = 0
                            logger.debug("Start Display job")
                            //ถ้า f ไม่เท่ากับ null แสดงว่ามีการทำงาน ให้หยุดรอ  20 วิ ถ้านานเกินก็ยกเลิกไปเลย
                            while (true) {
                                if (f.isDone) {
                                    logger.debug("Display is ok")
                                    break
                                }

                                TimeUnit.SECONDS.sleep(1)
                                i++
                                if (i >= 20) {
                                    f.cancel(true)
                                    logger.error("Display task timeout")
                                    break
                                }
                            }
                        }
                    } else {
                        logger.error("This job ${job} not finish")
                    }
                } catch (e: Exception) {
                    logger.error("Error : ${e.message}")
                }

                //ts.run(work)
            }
        }


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunDSDP::class.java)
    }
}