package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class RunPSDP(val pjs: PijobService, val js: JobService,
              val ts: TaskService, val ss: SensorService, val dss: DS18sensorService,
              val io: Piio, val dps: DisplayService, val rs: ReadUtil) {

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

                var ps = rs.readPressureByjob(job)
                if (ps != null) {
                    var rt = 0L
                    var runtime = job.runtime
                    if (runtime != null) {
                        rt = runtime.toLong()
                    }

                    var task = DPPS(ps.pressurevalue!!, dps, rt)
                    var f = ex.submit(task)
                    logger.debug("Task info AT:${ex.activeCount} PS:${ex.poolSize} " +
                            "CP:${ex.completedTaskCount} / T:${ex.taskCount}")
                    try {
                        var re = f.get(10, TimeUnit.SECONDS)
                        logger.debug("Run ok ${re}")
                    } catch (e: Exception) {
                        logger.error("1 ${e.message}")
                        f.cancel(true)
                    }
                }
            }
        }


    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("DPPS") //สำหรับแสดงผล

        if (job == null) {
            logger.error("Job not found DSDP")
            return null
        }

        var jobs = pjs.findJob(job.id)
        return jobs as List<Pijob>?

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunPSDP::class.java)
    }

}

class DPPS(var value: DS18value?, val dps: DisplayService, var displaytime: Long) : Callable<Boolean> {
    constructor(value: BigDecimal, dps: DisplayService, displaytime: Long) : this(null, dps, displaytime) {
        vv = value
    }

    var vv: BigDecimal? = null
    var df = DecimalFormat("#0.0")
    var d100 = DecimalFormat("000")
    override fun call(): Boolean {
        logger.info("Run DPPS")


        var d = "00@0"
        try {
            if (value != null) {
                d = df.format(value?.t)
                if (d.length > 4) {
                    d = "P" + d100.format(value?.t)
                }
                d = d.replace(".", "@")
            } else if (vv != null) {
                logger.debug("Pressure is : ${vv}")

                var n = vv?.setScale(1,RoundingMode.HALF_UP)
                if (vv?.compareTo(BigDecimal.ZERO)!! <= 0)
                    vv = BigDecimal.ZERO
                d = df.format(n)

                logger.debug("D ot display ${d}")
                 if (d.length > 4) {
                     d = "P" + d100.format(vv)
                 }
                d = d.replace(".", "@")
            }
        } catch (e: Exception) {
            logger.debug("ERROR ${e.message}")
            return false
        }


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
        try {
            dps.lockdisplay(this)
            dps.dot.print(d)
            TimeUnit.SECONDS.sleep(displaytime)
            dps.unlock(this)
            logger.debug("EndDPPS")
            logger.debug("DPPS Run is ok")
            return true
        } catch (e: Exception) {
            dps.unlock(this)
            logger.debug(e.message)
        }
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DPPS::class.java)
    }
}