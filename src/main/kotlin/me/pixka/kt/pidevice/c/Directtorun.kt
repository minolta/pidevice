package me.pixka.kt.pidevice.c

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.WarterLowPressureService
import me.pixka.kt.pidevice.worker.D1TWorkerII
import me.pixka.kt.pidevice.worker.NotifyPressureWorker
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.DWK
import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class Directtorun(
    val pijobService: PijobService, val task: TaskService,
    val mtp: MactoipService, val rs: ReadTmpService,val ntf:NotifyService,val lps:WarterLowPressureService
) {

    @CrossOrigin
    @RequestMapping(value = ["/directtorun/{id}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun directtorun(@PathVariable("id") id: Long) {

        try {
            var job = pijobService.findByRefid(id)

            if (job != null)
                if (!task.checkrun(job)) {
                    var j = getWorker(job)
                    if (j != null)
                        task.run(j)
                }
        }
        catch (e:Exception)
        {
            logger.error("Directtorun ${e.message}")
        }
    }

    fun getWorker(job: Pijob): PijobrunInterface? {

        try {
            if (job.job?.name.equals("runtbyd1")) {
                return D1TWorkerII(job, mtp, rs)
            }
            else if(job.job?.name.equals("runhbyd1"))
            {
                return D1hjobWorker(job,mtp,ntf,lps)
            }
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
        }

        return null
    }

    var logger = LoggerFactory.getLogger(Directtorun::class.java)

}