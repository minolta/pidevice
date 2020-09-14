package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.ReadStatusService
import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * สำหรับ notify worker
 */
class NotifyPressureWorker(var pijob: Pijob, var readStatusService: ReadStatusService,
                           var target: String, var notify: NotifyService) : PijobrunInterface, Runnable {
    var token = System.getProperty("pressurenotify")
    var status: String = ""
    var run: Boolean = false
    var runDate: Date? = null
    var psi:Double? =0.0
    var exitdate:Date?=null
    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
    }

    override fun runStatus(): Boolean {
        return run
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        return runDate
    }

    override fun state(): String? {
        return status
    }

    override fun setrun(p: Boolean) {
        run = p
    }

    override fun run() {
        try {
            runDate = Date()
            run = true
            status = "Status run"
            psi = readStatusService.readPSI(target, 2)
            if (psi != null) {
                var low = pijob.tlow?.toDouble()
                var high = pijob.thigh?.toDouble()

                if (low!! <= psi!! && psi!! <= high!!) {
                    status = "In Rang ${low}  <= ${psi} => ${high}"
                    if (token == null)
                        token = pijob.token
                    //in rang
                    var d:String? = ""
                    if(pijob.description!=null)
                        d = pijob.description
                    notify.message("Pressure In Rang ${low}  <= ${psi} => ${high} JOB:${pijob.name} description:${d}", token)
                }
                else
                {
                    status = "In Rang ${low}  <= ${psi} => ${high}"
                    logger.debug("Not in rang")
                }

            }
        } catch (e: Exception) {
            logger.error(e.message)
        }

//        run = false
        setEnddate()
        status = "End job"
    }
    fun setEnddate() {
        var t = 0L

        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.

        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            run = false//ออกเลย
    }
    fun psi(): Double? {
        return psi
    }
    companion object {
        internal var logger = LoggerFactory.getLogger(NotifyPressureWorker::class.java)
    }
}