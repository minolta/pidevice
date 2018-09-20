package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.DecimalFormat
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class RunDSDP(val pjs: PijobService, val js: JobService,
              val ts: TaskService, val ss: SensorService, val dps: DisplayService, val rs: ReadUtil) {

    val ex = ThreadPoolExecutor(
            2,
            10,
            10, // <--- The keep alive for the async task
            TimeUnit.SECONDS, // <--- TIMEOUT IN SECONDS
            ArrayBlockingQueue(100),
            ThreadPoolExecutor.AbortPolicy() // <-- It will abort if timeout exceeds
    )

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    fun run() {

        var jobs = loadjob()

        if (jobs != null) {
            for (job in jobs) {
                logger.debug("Run ${job.id}")

                var v = ss.readDsOther(job.desdevice_id!!, job.ds18sensor_id!!)
                logger.debug("Value ${v}")
                if (v != null) {
                    var task = DPT(v, dps)
                    var f = ex.submit(task)
                    logger.debug("Task info AT:${ex.activeCount} PS:${ex.poolSize} CP:${ex.completedTaskCount} / T:${ex.taskCount}")
                    try {
                        var re = f.get(10, TimeUnit.SECONDS)
                        logger.debug("Run ok ${re}")
                    } catch (e: Exception) {
                        logger.error("1 ${e.message}")
                        f.cancel(true)
                    }

                } else {
                    logger.error("Can not read ds18value")
                }
            }
        }


    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("DSDP") //สำหรับแสดงผล

        if (job == null) {
            logger.error("Job not found DSDP")
            return null
        }

        var jobs = pjs.findByDSDP(job.id)
        return jobs as List<Pijob>?

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunDSDP::class.java)
    }

}

class DPT(var value: DS18value, val dps: DisplayService) : Callable<Boolean> {
    var df = DecimalFormat("##.0")
    var d100 = DecimalFormat("###")
    override fun call(): Boolean {
        logger.info("Run DPT")


        var d = "0000"
        try {
            df.format(value.t)
            if (d.length > 4) {
                d = "*" + d100.format(value.t)
            }
        } catch (e: Exception) {
            logger.debug("ERROR ${e.message}")
            return false
        }

        if (value != null) {
            var count = 0
            while (dps.lock) {
                logger.debug("Wait for lock display")
                TimeUnit.MILLISECONDS.sleep(200)
                count++
                if (count >= 20) {
                    logger.error("Lock display time out")
                    return false
                }
            }
            dps.lockdisplay(this)
            dps.dot.print(d)
            dps.unlock(this)
            logger.debug("EndDPT")

        }

        logger.debug("DPT Run is ok")
        return true
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DPT::class.java)
    }
}