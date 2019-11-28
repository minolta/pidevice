package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.Workercounter
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Future

@Component
@Profile("pi")
class CounterOther(val context: ApplicationContext,
                   val pijobService: PijobService, val dss: DS18sensorService,
                   val js: JobService, val io: Piio, val ps: PortstatusinjobService,
                   val ts: TaskService,
                   val ss: SensorService, val gpio: GpioService, val dps: DisplayService, val ms: MessageService) {

    var runjobs = ArrayList<runInfo>()
    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun run() {
        try {
            logger.debug("Start find Counter job")

            var jobs = loadjob()
            var torun = find(jobs!!)


            if (torun != null) {
                logger.debug("counter for run ${torun.size}")
                for (job in torun) {
                    logger.debug("Start counterjob ${job.id}")
                    var task = Workercounter(job, ps, gpio, ss, dps, ms, io, dss)

                    if (!ts.run(task)) {
                        logger.error("Reject job #threadinfo ${task}")
                    } else {
                        logger.info("runcounterjob ${task}")
                    }
                }

            } else {
                logger.error("Not have job to runcounter")
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    /**
     * เคลียที่ run หมดอยู่แล้ว
     */
    @Scheduled(fixedDelay = 10000)
    fun clear() {

        for (run in runjobs) {
            var f = run.f
            if (f?.isDone!!) {
                logger.info("Remove old job")
                runjobs.remove(run)
            }
        }
    }

    fun loadjob(): List<Pijob>? {
        try {
            var counter = js.findByName("Counter")
            logger.debug("JOB for run ${counter}")
            var jobs = pijobService.findByCounter(counter?.id!!)
            return jobs
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    /**
     * ใช้สำหรับตรวจสอบ
     */
    fun checkcanrun(jobs: List<Pijob>): ArrayList<Pijob> {

        var canrun = ArrayList<Pijob>()
        for (job in jobs) {

            if (!injob(job)) {
                canrun.add(job)
            }

        }

        return canrun
    }

    /**
     * ตรวจสอบว่า มีอยู่ใน list แล้วหรือยัง
     */
    fun injob(job: Pijob): Boolean {
        for (info in runjobs) {
            var p = info.job
            if (p?.runStatus() == true && p.getPijobid().toInt() == job.id.toInt())
                return true
        }
        return false
    }

    /**
     * หา job ที่จะจับเวลา
     */
    fun find(jobs: List<Pijob>): ArrayList<Pijob>? {
        logger.debug("Start find job")
        var buf = ArrayList<Pijob>()
        if (jobs != null) {
            for (job in jobs) {
                logger.debug("Job for run ${job}")
                var low = job.tlow?.toInt()
                var high = job.thigh?.toInt()
                var senid = job.ds18sensor_id
                var desid = job.desdevice_id
                var value: DS18value? = null


                try {
                    value = ss.readDsOther(desid!!, senid!!)
                    logger.debug("Other value ======>${value}")
                } catch (e: Exception) {
                    logger.error("Read other error ${e.message}")
                }

                if (value == null) {
                    var localsensor = dss.find(job.ds18sensor_id)
                    logger.debug("Found local sensor ? ${localsensor}")
                    if (localsensor != null) {
                        var v = io.readDs18(localsensor.name!!)
                        value = DS18value()
                        value.t = v


                    }
                }
                logger.debug(" FIND Value: ${value}")
                if (value != null) {
                    var v = value.t?.toInt()
                    if (v!! >= low!! && v <= high!!) {
                        //ถ้าอยู่ในช่วงจะทำการนับ
                        buf.add(job)
                    } else {
                        logger.error("Value not in range  ${low} < ${value.t} > ${high}")
                    }
                } else {
                    logger.error("Value is null ${job}")
                }


            }
            return buf
        }

        logger.error("Not have job to run")
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CounterOther::class.java)
    }
}

class runInfo(var job: PijobrunInterface? = null, var f: Future<*>? = null, var runtime: Date? = Date())
