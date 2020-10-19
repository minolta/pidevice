package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
class CheckTimeService(val lgs: LogService) {

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

    /**
     * สำหรับตรวจสอบ เวลาที่ส่งเข้ามา job นี้สามารถทำงานได้หรือเปล่า
     */
    fun checkTime(job: Pijob, now: Date): Boolean {
        try {
            if(job.stimes == null && job.etimes == null)
                return true // not set time rang
            var n = df.parse(df.format(now))
            var nl = n.time

            //set ช่วงเวลา
            if (job.stimes != null && job.etimes != null) {
                var s = df.parse(job.stimes)
                var e = df.parse(job.etimes)
                var sl = s.time
                var el = e.time
                logger.debug("${job.name}  RUNOFFPUMP ${sl} <= ? ${nl} <= ${el}")
                if (nl in sl..el)
                    return true
            } else
            //แบบถ้าเวลาที่ส่งมาหลังจากนี้จะทำงาน
                if (job.stimes != null && job.etimes == null) {
                    var s = df.parse(job.stimes)
                    var sl = s.time
                    logger.debug("${job.name} RUNOFFPUMP ${s} <= ? ${n} ")
                    if (sl <= nl)
                        return true
                } else
                //ทดสอบแบบก่อนเวลา
                    if (job.stimes == null && job.etimes != null) {
                        var e = df.parse(job.etimes)
                        var el = e.time

                        logger.debug("${job.name} RUNOFFPUMP  ${n} <= ${e}")
                        if (nl <= el)
                            return true
                    }
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "CheckTimeService", "", "", "",
                    job.desdevice?.mac, job.refid)
            logger.error("ERROR  ${e.message} JOB: ${job.name}")
        }

        return false
    }

    fun checkTime(st: String?, et: String?, now: Date, jobname: String): Boolean {
        //set ช่วงเวลา
        var n = df.parse(df.format(now))
        if (st != null && et != null) {
            var s = df.parse(st)
            var e = df.parse(et)

            logger.debug("${jobname}  RUNOFFPUMP ${s.time} <= ? ${n.time} <= ${e.time}")
            if (s.time <= n.time && n.time <= e.time)
                return true
        }
        //แบบถ้าเวลาที่ส่งมาหลังจากนี้จะทำงาน
        if (st != null && et == null) {
            var s = df.parse(st)
            logger.debug("${jobname} RUNOFFPUMP ${s} <= ? ${n} ")
            if (s.time <= n.time)
                return true
        }
        //ทดสอบแบบก่อนเวลา
        if (st == null && et != null) {
            var e = df.parse(et)
            logger.debug("${jobname} RUNOFFPUMP  ${n} <= ${e}")
            if (n.time <= e.time)
                return true
        }
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CheckTimeService::class.java)
    }
}