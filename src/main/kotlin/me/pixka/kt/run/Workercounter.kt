package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period
import org.joda.time.Seconds
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Profile("pi")
class Workercounter(var pijob: Pijob, var gpio: GpioService, val ss: SensorService, val dps: DisplayService, val ms: MessageService) : Runnable, PijobrunInterface {

    var run: Long? = null
    var timeout: Long? = 10000 //สำหรับหมดเวลา
    var startdate: Date? = null
    var runtime: Date? = null
    var completerun: Int? = 0 //เวลาที่ run ไปแล้ว
    var finishrun: Date? = null //เวลาที่ เสร็จ
    var isRun = true
    var period: Period? = null
    override fun getPJ(): Pijob {
        return pijob
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun setG(gpios: GpioService) {
        this.gpio = gpios
    }

    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    var messageto = false
    override fun run() {

        logger.debug("Start run counter job ID ***${pijob.id}***")
        var timeoutcount = 0
        while (true) {
            var desid = pijob.desdevice_id
            var sensorid = pijob.ds18sensor_id

            var value = ss.readDsOther(desid!!, sensorid!!)

            logger.debug("Read value : ${value}")
            if (value != null) {


                var v = value.t?.toInt()
                var l = pijob.tlow?.toInt()
                var h = pijob.thigh?.toInt()
                if (v!! >= l!! && v!! <= h!!) {

                    logger.debug("Value in range ${pijob.tlow} <= ${v} => ${pijob.thigh}")
                    if (startdate == null) {
                        startdate = Date()
                        timeout = pijob.waittime //
                        run = pijob.runtime //เวลาในการ run เอ็นวินาที
                        finishrun = DateTime().plusSeconds(pijob.runtime?.toInt()!!).toDate() //เวลาเสร็จ
                    }
                    if (!messageto) {
                        messageto = true
                        ms.message("Start Counter 90  Close : ${finishrun}", "info")
                    }

                    //runtime = Date()

                    var rt = DateTime()
                    var st = DateTime(startdate)

                    runtime = rt.toDate()
                    period = Interval(st, rt).toPeriod() //ช่วงเวลา

                    var r = Seconds.secondsBetween(st, rt)
                    var havetorun = run?.toInt()!!
                    completerun = r.seconds
                    if (r.seconds >= havetorun) {
                        logger.debug("End run counter job ID ***${pijob.id}***")
                        messageto = false
                        ms.message("Interrup ", "info")
                        isRun = false
                        break
                    }

                    //var c = rt.minus(startdate?.time!!) //เวลาที่ run ไปแล้ว


                    //var d = startdate?.time!! - runtime?.time!! / 1000 // เวลาที่ทำงานไปแล้ว เป็นวินาที

                } else {
                    logger.error("Value not in range range ${pijob.tlow} <= ${v} => ${pijob.thigh} ")
                    timeoutcount++
                    if (timeoutcount >= timeout?.toInt()!!) {
                        logger.error("Time out count exit ${timeoutcount}")
                        ms.message("Tmp is under rang exits count", "error")
                        break
                    }

                }
            }


            try {
                display()
            } catch (e: Exception) {
                logger.error("Display error ${e.message}")
            }
            logger.debug("Counter Wait in 1 minits")
            TimeUnit.MINUTES.sleep(1)
        }
    }

    var df = SimpleDateFormat("HH:mm:ss")

    fun display() {
        logger.debug("Display Counter info Compilete run ${completerun} in sec end run AT: ${finishrun}  ")
        var count = 0
        while (dps.lock) {

            TimeUnit.MILLISECONDS.sleep(100)
            logger.debug("Wait for lock display lock by ==> ${dps.obj}")
            count++
            if (count >= 500) {
                logger.error("Lock display timeout")
                break
            }
        }

        if (!dps.lock) {
            try {
                logger.debug("Start lock display ")
                var display = dps.lockdisplay(this)
                display.showMessage("Counter in ${completerun} Sec Close AT: ${df.format(finishrun)} ")
                logger.debug("Display ok.")
            } catch (e: Exception) {
                logger.error("Error: ${e.message}")
            } finally {
                if (dps.lock)
                    dps.unlock(this)
            }


        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Workercounter::class.java)
    }
}