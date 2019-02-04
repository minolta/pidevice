package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.kt.pidevice.u.ReadUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * เป็นตัว run การนับว่าจะให้ทำอะไร
 */
@Profile("pi")
class CountdownWorkerii(var pijob: Pijob, var gpios: GpioService, val sensorService: SensorService,
                        val notifyService: NotifyService, val readUtil: ReadUtil)
    : PijobrunInterface, Runnable {

    var runtime: Int = 0
    var isRun = false
    var startDate: Date? = null
    val d = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val dn = SimpleDateFormat("yyyy/MM/dd")
    var state = ""
    override fun run() {
        if (!checkincondition()) {
            logger.error("Not in rang end this job")
            state = "Not in rang end this job"
            return
        }
        isRun = true
        setup()
        startDate = Date()

        var timeout = 0
        var canrunport = false
        //start run
        while (true) {

            ///ถ้าขึ้นถึงก็เริ่มนับไปเลย

            if (!checkincondition()) {
                timeout++
                if (timeout >= 600) // ถ้าไม่อยู่ในเงือนไข 10 นาที ก็จบการทำงานเลย
                {
                    logger.error("Error Timeout 10 min")
                    state = "Error Timeout 10 min"
                    notifyService.message("Time out 600 sec ${pijob.name}")
                    isRun = false
                    break
                }
            } else {
                timeout--
            }

            if (checkruncomplete()) {
                canrunport = true
                logger.info("To run port")
                state = "To run port"
                break
            }

            TimeUnit.SECONDS.sleep(1)
            /*   runtime--
               TimeUnit.SECONDS.sleep(1)
               if (runtime <= 0) {//ถ้า นับมาจนครบ ก็เริ่มทำงาน
                   canrunport = true
                   logger.info("To run port")
                   state = "To run port"
                   break
               }
               */


        }


        //ถ้ามีการกำหนด เวลา เริ่มต้น
        if (pijob.stime != null || pijob.stimes != null) {
            checktime() // รอจนกว่าจะถึงเวลาเริ่มทำงาน
        }

        try {
            if (canrunport) {
                runport()
            }
        } catch (e: Exception) {
            logger.error("Run port error ${e.message}")
            state = "Error ${e.message}"
            notifyService.message("${e.message}")
        }



        logger.info("Endcountdown")
        state = "End countdown"
        notifyService.message("End countdown")
        isRun = false

    }

    var finishtime: Date? = null
    fun checkruncomplete(): Boolean {
        try {
            if (pijob.stimes == null)
                return true
            if (finishtime == null)
                finishtime = getnextrunt()

            var now = Date()

            logger.debug("Checktime ${finishtime?.time} ${now.time} ")
            if (now.time >= finishtime?.time!!) {
                return true
            }

            return false
        } catch (e: Exception) {
            logger.error(e.message)
            notifyService.message("${pijob.name} error ${e.message}")
            throw e
        }

    }

    /**
     * การ runport จะเปิด port ตามกำหนด และ
     * ใช้ค่า  waittime สำหรับรอ
     */
    fun runport() {

        var timetorun = 1
        if (pijob.timetorun != null && pijob.timetorun!!.toInt() > 0)
            timetorun = pijob.timetorun!!.toInt()


        logger.debug("Time to run ${timetorun}")
        var portlist = pijob.ports

        logger.debug("Port to run ${portlist}")
        notifyService.message("${pijob.name} Port to run ")
        if (portlist != null) {
            while (timetorun > 0) {
                timetorun--

                for (port in portlist) {

                    try {
                        var runtime = getRuntime(port)
                        var waittime = getWaittime(port)

                        var pin = gpios.getDigitalPin(port.portname?.name!!)
                        var logic = port.status!!.name
                        var l = true
                        if (logic!!.toLowerCase().indexOf("false") != -1) {
                            l = false
                        }

                        logger.debug("Port to run ${pin} logic ${logic} Run time: ${runtime} waittime: ${waittime}")
                        state = "set Pin ${pin} to ${l}"

                        gpios.setPort(pin!!, l)
                        state = "Run this port ${runtime} "
                        TimeUnit.SECONDS.sleep(runtime!!)
                        gpios.setPort(pin, !l)
                        state = "wait ${waittime}"
                        TimeUnit.SECONDS.sleep(waittime!!)
                    } catch (e: Exception) {
                        logger.error("Run port ERROR ${e.message}")
                    }
                }


            }
        } else {
            logger.error("No port to run")
            notifyService.message("No port to run")
        }

    }

    fun getRuntime(port: Portstatusinjob): Long? {
        var runtime = pijob.runtime

        if (port.runtime != null)
            runtime = port.runtime!!.toLong()

        return runtime

    }

    fun getWaittime(port: Portstatusinjob): Long? {
        var waittime = pijob.waittime
        if (port.waittime != null) {
            waittime = port.waittime!!.toLong()
        }
        return waittime
    }

    fun checktime() {
        try {
            logger.debug("checktime Have to check if pijob set stimes system have to wait util stimes ")
            if (pijob.stimes != null) {
                var timetorun = getnextrunt()

                while (true) {
                    var now = Date().time
                    logger.debug("cooldown checktime checktime nextrun time wait time :  ${timetorun!!.time} now ${now}")
                    state = "cooldown checktime checktime nextrun time wait time :  ${timetorun.time} now ${now}"
                    if (now >= timetorun.time) {
                        break
                    }
                    state = "Wait 1 sec"
                    TimeUnit.SECONDS.sleep(1)
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    fun getnextrunt(): Date? {
        try {
            dn.timeZone = TimeZone.getTimeZone("UTC")
            var ds = dn.format(Date())
            logger.debug("DS: ${ds}")
            state = "DS : ${ds}"
            var datenow = dn.parse(ds)
            val c = Calendar.getInstance()
            c.time = datenow
            c.add(Calendar.DATE, 1)  // number of days to add
            var nextdate = dn.format(c.time)
            var timetorun = d.parse(nextdate + " " + pijob.stimes)
            logger.debug("Next run ${timetorun}")
            state = "Next run ${timetorun}"
            return timetorun
        } catch (e: Exception) {
            logger.error("coundown getnextrun pares date ${e.message}")
            state = "coundown getnextrun pares date ${e.message}"
            throw e

        }

    }

    //ใช้สำหรับอ่านค่า ว่าอยู่ในช่วงหรือเปล่า
    fun checkincondition(): Boolean {
        if (pijob != null) {
            try {
                var value = readUtil.readTmpByjob(pijob)
                state = "Read value ${value}"
                logger.debug("Readvalue ${value} ")
                if (value != null) {
                    var v = value.toFloat()
                    var tl = pijob.tlow!!.toFloat()
                    val th = pijob.thigh!!.toFloat()
                    if (v >= tl && v <= th) {
                        state = "Job in range"
                        logger.debug("Job in rang")
                        return true
                    }
                    state = "job not in rang"
                    logger.debug("Job not in rang")
                    return false

                }
                return false
            } catch (e: Exception) {

                logger.error(e.message)
                state = e.message!!
                throw e
            }


        }
        state = "Pijob is null"
        return false
    }

    fun readktype(): Boolean {
        try {
            var value = sensorService.readDsOther(pijob.desdevice_id!!, null)
            state = "Value from Ktype ${value}"
            if (value == null || value.t == null) {
                logger.error("Value Ktype is null")
                return false
            }
            if (value != null) {
                var v = value.t!!.toFloat()
                var tl = pijob.tlow!!.toFloat()
                val th = pijob.thigh!!.toFloat()

                if (v >= tl && v <= th)
                    return true
                return false

            }
            return false
        } catch (e: Exception) {
            logger.error("Read k type ${e.message}")
            throw e
        }
    }

    fun readother(): Boolean {
        try {
            var value = sensorService.readDsOther(pijob.desdevice_id!!, pijob.ds18sensor_id)
            state = "Value from readOther ${value}"
            if (value == null || value.t == null) {
                logger.error("Value is null")
                return false
            }
            if (value != null) {
                var v = value.t!!.toFloat()
                var tl = pijob.tlow!!.toFloat()
                val th = pijob.thigh!!.toFloat()

                if (v >= tl && v <= th)
                    return true
                return false

            }
            return false
        } catch (e: Exception) {
            logger.error("Read other ${e.message}")
            throw e
        }
    }

    fun setup() {

        if (pijob != null && pijob.runtime != null) {
            runtime = pijob.runtime!!.toInt()
        } else {
            isRun = false
            state = "No runtime information"
            throw Exception("No runtime information")
        }


    }


    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        this.gpios = gpios
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        if (pijob != null) {
            return pijob.id
        }
        return 0L
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return startDate
    }

    override fun state(): String? {
        return state
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CountdownWorkerii::class.java)
    }

}