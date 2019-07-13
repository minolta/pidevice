package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pibase.t.HttpGetTask
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
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.*

@Component
@Profile("pi")
class RunDSDP(val pjs: PijobService, val js: JobService, val ips: IptableServicekt,
              val ts: TaskService, val ss: SensorService, val dss: DS18sensorService,
              val io: Piio, val dps: DisplayService, val rs: ReadUtil) {
    val om = ObjectMapper()
    val ex = ThreadPoolExecutor(
            2,
            10,
            10, // <--- The keep alive for the async task
            TimeUnit.SECONDS, // <--- TIMEOUT IN SECONDS
            ArrayBlockingQueue(100),
            ThreadPoolExecutor.AbortPolicy() // <-- It will abort if timeout exceeds
    )

    fun readKtype(job: Pijob) {
        var ip = ips.findByMac(job.desdevice?.mac!!)
        if (ip != null) {
            try {
                var t = Executors.newSingleThreadExecutor()
                var url = "http://${ip.ip}/ktype"
                logger.debug("Read value ${url}")
                var get = HttpGetTask(url)
                var tt = t.submit(get)

                var displaytime = 0L
                if (job.runtime != null) {
                    displaytime = job.runtime?.toLong()!!
                }
                var re = tt.get(5, TimeUnit.SECONDS)
                var ds = om.readValue<DS18value>(re, DS18value::class.java)

                logger.debug("Value ${ds}")
                var task = DPT(ds.t!!, dps, displaytime)
                var f = ex.submit(task)
                var display=    f.get(5, TimeUnit.SECONDS)

                logger.debug("Display ${display}")
            } catch (e: Exception) {

            }
        }
    }

    fun readValue(job: Pijob): DS18value? {
        try {
            var v: DS18value? = null
            try {
                v = ss.readDsOther(job.desdevice_id!!, job.ds18sensor_id!!)
                return v
            } catch (e: Exception) {
                logger.error("Read other error ${e.message}")
            }

            return v
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun readLocal(job: Pijob) {
        try {
            logger.debug("Read local  ID ${job.id}")
            var s = dss.find(job.ds18sensor_id)
            logger.debug("Found Sensor !! ${s}")
            if (s != null) {
                var tmp = io.readDs18(s.name!!)
                logger.debug("Read loacal value ${tmp}")
                // var tmp = rs.readTmpByjob(job)
                if (tmp != null) {
                    var runtime = job.runtime
                    var displaytime = 0L
                    if (runtime != null) {
                        displaytime = runtime.toLong()
                    }
                    var task = DPT(tmp, dps, displaytime)
                    var f = ex.submit(task)
                    logger.debug("Task info AT:${ex.activeCount} PS:${ex.poolSize} CP:${ex.completedTaskCount} / T:${ex.taskCount}")
                    try {
                        var re = f.get(10, TimeUnit.SECONDS)
                        logger.debug("Run ok ${re} ${job.id}")
                    } catch (e: Exception) {
                        logger.error("1 ${e.message}")
                        throw e
                        //f.cancel(true)
                    }
                }
            }

        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    fun run() {
        logger.debug("Run ${Date()}")
        var jobs = loadjob()
        logger.debug("JOB ${jobs}")
        if (jobs != null) {
            for (job in jobs) {

                readKtype(job)
//                logger.debug("Run ${job.id}")
//
//                var v: DS18value? = readValue(job)
//
//
//                logger.debug("Value ${v}")
//
//
//                if (v != null) {
//                    var runtime = job.runtime
//                    var displaytime = 0L
//                    if (runtime != null) {
//                        displaytime = runtime.toLong()
//                    }
//                    var task = DPT(v, dps, displaytime)
//                    var f = ex.submit(task)
//                    logger.debug("Task info AT:${ex.activeCount} PS:${ex.poolSize} CP:${ex.completedTaskCount} / T:${ex.taskCount}")
//                    try {
//                        var re = f.get(10, TimeUnit.SECONDS)
//                        logger.debug("Run ok ${re}")
//                    } catch (e: Exception) {
//                        logger.error("1 ${e.message}")
//                        //  f.cancel(true)
//                    }
//
//                } else {
//                    //logger.error("Can not read ds18value")
//                    //read loacl
//                    readLocal(job)
//                }
            }
        } else {
            logger.error("Job not found")
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

class DPT(var value: DS18value?, val dps: DisplayService, var displaytime: Long) : Callable<Boolean> {
    constructor(value: BigDecimal, dps: DisplayService, displaytime: Long) : this(null, dps, displaytime) {
        vv = value
    }

    var vv: BigDecimal? = null
    var df = DecimalFormat("##.0")
    var d100 = DecimalFormat("###")
    override fun call(): Boolean {
        logger.info("Run DPT")
        var d = "0000"
        try {
            var dd: BigDecimal? = null
            if (value != null) {
                d = df.format(value?.t)
                if (d.length > 4) {
                    d = "*" + d100.format(value?.t)
                }
                d = d.replace(".", "#")
            } else if (vv != null) {
                d = df.format(vv)
                if (d.length > 4) {
                    d = "*" + d100.format(vv)
                }
                d = d.replace(".", "-")
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
            logger.debug("EndDPT")
            logger.debug("DPT Run is ok")
            return true
        } catch (e: Exception) {
            logger.debug(e.message)
            dps.unlock(this)
            throw e
        }

        return true

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DPT::class.java)
    }
}