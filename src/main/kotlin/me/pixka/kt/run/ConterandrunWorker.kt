package me.pixka.kt.run

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.PortstatusinjobService
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period
import org.joda.time.Seconds
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.util.*
import java.util.concurrent.TimeUnit

@Profile("pi", "lite")
class CounterandrunWorker(var pj: Pijob, var gi: GpioService, val m: MessageService,
                          val i: Piio, val ppp: PortstatusinjobService, val ss: SensorService,
                          val dss: DS18sensorService, val read: ReadUtil)
    : Worker(pj, gi, i, ppp) {
    var run: Long? = null
    var runtime: Date? = null
    var startdate: Date? = null
    var timeout: Long = 10000 //สำหรับหมดเวลา
    var currenttimeout: Long = 0
    var finishrun: Date? = null //เวลาที่ เสร็จ
    var period: Period? = null
    var runcomplete = false
    override fun run() {
        logger.debug("Start run this job")
        state = "Start run " + Date()
        runcomplete = false
        getTime()
        while (true) {
            var desid = pijob.desdevice_id
            var sensorid = pijob.ds18sensor_id
            //อ่านจาก DS เครื่องอื่น
            if (sensorid == null || desid == null) {
                // Error job not complete informat
                state = "ERROR Job not complete information"
                isRun = false
                logger.error("ERROR Job not complete information")
                break
            }

            var value = read.readTmpByjob(pj)
            //read(sensorid, desid)
            logger.debug("Read value : ${value}")
            state = "Read value :${value}"

            if (value != null) {
                //ถ้ามีข้อมูล
                state = "Start run"

                var v = value.toFloat()
                var l = pijob.tlow?.toFloat()
                var h = pijob.thigh?.toFloat()

                logger.debug("Value for check ${l} > ${v} < ${h}")
                state = "Value for check ${l} > ${v} < ${h}"
                if (v != null && l != null && h != null) {
                    var c = check(v, l, h)
                    if (c) {
                        var completewait = checkrun()
                        if (completewait) {
                            state = "Run complete"
                            runcomplete = true
                            break
                        } else {
                            logger.debug("Time this run not complete ")
                        }
                        //ยังอยู่ในการทำงานอยู่นับต่อไป
                    } else {
                        // not in rang จบงาน
                        // ไม่อยู่ในเงือนไข มาตรวจสอบ timeout
                        currenttimeout++
                        logger.debug("Not in range ")
                        if (currenttimeout >= timeout) {
                            isRun = false
                            state = "Value not in Rang"
                            break;
                        }
                    }
                }
            }
            logger.debug("Counter Wait in 1 sec")
            TimeUnit.SECONDS.sleep(1)
        }


        if (runcomplete) {
            runPort(pijob)
        }
    }

    /**
     * Run port จะเอาค่า run กับ timeout จาก Hlow และ HHigh
     */
    fun runPort(pijob: Pijob) {
        try {
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
            logger.debug("Start run port ${ports}")
            var runtime = pijob.hlow?.toLong()
            var nextrun = pijob.hhigh?.toLong()
            if (ports != null && ports.size > 0) {
                setport(ports)
                if (runtime != null) {
                    state = "Run time ${runtime}"
                    logger.debug("Run to ${runtime}")
                    TimeUnit.SECONDS.sleep(runtime)
                    logger.debug("Next run")
                }

                resetport(ports)
                if (nextrun != null) {
                    logger.debug("Wait time ${nextrun}")
                    state = "Wait time ${nextrun}"
                    TimeUnit.SECONDS.sleep(nextrun)
                }
                state = "Run port is end"
                logger.debug("Run port is end")


            } else {
                logger.debug("Not have port to run")
            }
        } catch (e: Exception) {
            logger.error("run PORT ${e.message}")
            throw e
        }

    }

    fun getTime() {
        try {
            if (startdate == null) {
                startdate = Date()
                timeout = pijob.waittime!! //
                run = pijob.runtime //เวลาในการ run เอ็นวินาที
                finishrun = DateTime().plusSeconds(pijob.runtime?.toInt()!!).toDate() //เวลาเสร็จ
                logger.debug("Counter info START : ${startdate} RUN TIME: ${run} End run ${finishrun} open port ${pijob.hlow}")
                state = "Counter info START : ${startdate} RUN TIME: ${run} End run ${finishrun} open port ${pijob.hlow}"
            }
        } catch (e: Exception) {
            logger.error("GETTIME: ${e.message}")
            state = "Error ${e.message}"
            isRun = false
            throw e
        }
    }

    fun checkrun(): Boolean {

        try {
            state = "Check run is complete"
            var rt = DateTime()
            var st = DateTime(startdate)

            runtime = rt.toDate()
            period = Interval(st, rt).toPeriod() //ช่วงเวลา

            var r = Seconds.secondsBetween(st, rt)
            logger.debug("Run in ${r}")
            state = "Run in ${r} timeout ${run}"
            var havetorun = run?.toInt()!!
            if (r.seconds >= havetorun) {
                logger.debug("Wait timeout")
                state = "End wait Run set port"
                return true
            }
            logger.debug("Still run this job")
            state = "Still wait ${Date()}"
            return false
        } catch (e: Exception) {
            logger.error("Checktime ${e.message}")
            state = "Checktime ${e.message}"
            isRun = false
            throw e
        }


    }

    /**
     * จะใช้สำหรับ ดูว่าการนับ ของ job จะทำงานงานได้อยู่เปล่า
     */
    fun check(v: Float, l: Float, h: Float): Boolean {
        if (v >= l && v <= h) {
            return true
        }
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CounterandrunWorker::class.java)
    }
}


