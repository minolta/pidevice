package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import java.text.SimpleDateFormat
import java.util.*


open class DWK(var pijob: Pijob, var status: String = "",
               var isRun: Boolean = false, var startRun: Date = Date(),
               var exitdate: Date? = null) : PijobrunInterface {


    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        return pijob?.id!!
    }

    override fun getPJ(): Pijob {
        return pijob!!
    }

    override fun startRun(): Date? {
        return startRun
    }

    override fun state(): String? {
        return status
    }

    override fun setrun(p: Boolean) {

        isRun = p
    }

    override fun exitdate(): Date? {
        return exitdate
    }

    var df = SimpleDateFormat("HH:mm")
    fun findExitdate(job: Pijob): Date? {
        var tvalue: Long? = 0L
        if (job.waittime != null)
            tvalue = job.waittime!!
        if (tvalue != null) {
            if (job.runtime != null)
                tvalue += job.runtime!!
        }
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, tvalue!!.toInt())
        return calendar.time
    }
}
