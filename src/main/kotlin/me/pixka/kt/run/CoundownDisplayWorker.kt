package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.NotifyService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * เป็นตัว run การนับว่าจะให้ทำอะไร สำหรับ แสดงผล กำหนดเวลาปิด จาก hlow เป็น วินาที
 */
class CountdownDisplayWorker(var pijob: Pijob,
                             val sensorService: SensorService,
                             val display: DisplayService,val notifyService: NotifyService) : PijobrunInterface, Runnable {
    val queue = ThreadPoolExecutor(5, 10, 30,
            TimeUnit.MINUTES, LinkedBlockingDeque<Runnable>(50),
            ThreadPoolExecutor.CallerRunsPolicy())
    var runtime: Int = 0
    var isRun = false
    var startDate: Date? = null
    val d = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val dn = SimpleDateFormat("yyyy/MM/dd")
    var closedate: Date? = null
    var runcount = 0
    var state = ""
    var df = SimpleDateFormat("HH:mm:ss")
    override fun run() {
        if (!checkincondition()) {
            logger.error("Not in rang end this job")
            state = "Not in rang end this job"
            return
        }
        logger.debug("Start countdown ")

        isRun = true
        setup()
        startDate = Date()
        state = "Start countdown ${startDate}"
        notifyService.message("Start countdown at ${startDate}  เวลาปิด ${closedate}")
        var timeout = 0


        while (runtime > 0) {
            TimeUnit.SECONDS.sleep(1)
            runtime--
            runcount++
            if (runtime % 60 == 0) {
                display()
            }
            if(runtime % 120 == 0)
            {
                notifyService.message("เวลาปิดเตา ${closedate} Job name ${pijob.name}")
            }
            if (!checkincondition()) {
                timeout++
                if (timeout >= 300) //ถ้าหลุดนานกว่า 5 นาที
                {
                    notifyService.message("Countdown display Time out Job ${pijob.name}")
                    break
                }
            } else {
                timeout--
            }

            state = "Counter ${runtime}"
        }

        isRun = false

        if (pijob.waittime != null) {
            state = " Run waittime ${pijob.waittime}"
            TimeUnit.SECONDS.sleep(pijob.waittime!!)
        }

        logger.info("End countdowndisplay")
        state = "End countdowndisplay"
    }

    fun display() {
        try {
            var task = DisplayTask(display, "Run ${runcount} Close AT:     ${df.format(closedate)}         ")
            logger.debug("Run ${runcount} Close AT:     ${df.format(closedate)}         ")
            state = "Run ${runcount} Close AT:     ${df.format(closedate)}         "
            var f = queue.submit(task)
            f.get(5, TimeUnit.SECONDS)
            logger.debug("Run display complete")
        } catch (e: Exception) {
            logger.error("Display Error ${e.message}")

        }
    }

    //ใช้สำหรับหา ว่า จะปิดกี่โมงจาก Runtime
    fun findCloseDate() {

    }

    fun checktime() {
        if (pijob.stimes != null) {

            var timetorun = getnextrunt()

            while (true) {
                var now = Date().time

                logger.debug("cooldown checktime checktime nextrun time wait time :  ${timetorun!!.time} now ${now}")
                if (now.toInt() >= timetorun.time.toInt()) {
                    break
                }

                TimeUnit.SECONDS.sleep(1)
            }
        }
    }

    fun getnextrunt(): Date? {
        try {
            var ds = dn.format(Date())
            println("DS: ${ds}")
            var datenow = dn.parse(ds)
            val c = Calendar.getInstance()
            c.time = datenow
            c.add(Calendar.DATE, 1)  // number of days to add
            var nextdate = dn.format(c.time)
            var timetorun = d.parse(nextdate + " " + pijob.stimes)
            state = "Next run ${timetorun}"
            return timetorun
        } catch (e: Exception) {
            logger.error("coundown getnextrun pares date ${e.message}")
            state = "coundown getnextrun pares date ${e.message}"
        }
        return null
    }

    //ใช้สำหรับอ่านค่า ว่าอยู่ในช่วงหรือเปล่า
    fun checkincondition(): Boolean {
        if (pijob != null) {
            if (pijob.desdevice_id != null && pijob.ds18sensor_id != null) {
                return readother()
            } else if (pijob.desdevice_id != null) {
                return readktype()
            }


        }
        return false
    }

    fun readktype(): Boolean {
        var value = sensorService.readDsOther(pijob.desdevice_id!!, null)
        if (value == null || value.t == null)
            return false

        if (value != null) {
            var v = value.t!!.toFloat()
            var tl = pijob.tlow!!.toFloat()
            val th = pijob.thigh!!.toFloat()

            if (v >= tl && v <= th)
                return true
            return false

        }
        return false
    }

    fun readother(): Boolean {
        var value = sensorService.readDsOther(pijob.desdevice_id!!, pijob.ds18sensor_id)
        if (value == null || value.t == null)
            return false

        if (value != null) {
            var v = value.t!!.toFloat()
            var tl = pijob.tlow!!.toFloat()
            val th = pijob.thigh!!.toFloat()

            if (v >= tl && v <= th)
                return true
            return false

        }
        return false
    }

    fun setup() {

        if (pijob != null && pijob.runtime != null) {
            runtime = pijob.runtime!!.toInt()
            closedate = DateTime().plusSeconds(pijob.runtime?.toInt()!!).toDate()
            logger.debug("Runtime ${runtime} ${closedate}")
            state = "Runtime ${runtime} ${closedate}"
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
        //this.gpios = gpios
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
        internal var logger = LoggerFactory.getLogger(CountdownDisplayWorker::class.java)
    }

}

class DisplayTask(val display: DisplayService, val msg: String) : Runnable {
    override fun run() {

        try {
            var dot = display.lockdisplay(this)
            if (dot != null) {
                logger.debug("Display ${msg}")
                dot.showMessage(msg)
                display.unlock(this)
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(DisplayTask::class.java)
    }

}