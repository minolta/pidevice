package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
class CheckTimeService(var lgs: LogService?=null) {

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

            if (job.stimes.isNullOrEmpty() && job.etimes.isNullOrEmpty())
                return true // not set time rang
            var n = df.parse(df.format(now))
            var nl = n.time


            if(job.stimes!=null && job.etimes!=null && (job.stimes!!.isNotEmpty() && job.etimes!!.isNotEmpty()))
            {
                try {
                    var s = df.parse(job.stimes)
                    var e = df.parse(job.etimes)
                    if(s.time<= nl && nl<=e.time)
                        return true
                    return false
                }catch (e:Exception)
                {
                    logger.error("Parse ERROR ${job.name} ${job.stimes} ${job.etimes}")
                }
            }
            //หาว่าเวลาก่อนหน้าที่กำหนดหรือเปล่า
            if (job.stimes.isNullOrEmpty() && job.etimes!=null && job.etimes!!.isNotEmpty()) {
                var e = df.parse(job.etimes)
                var el = e.time

                if (nl <= el)
                    return true

                return false
            }

            if(job.stimes!=null && job.etimes.isNullOrEmpty() && job.stimes!!.isNotEmpty())
            {
                var e = df.parse(job.etimes)
                var el = e.time

                if (nl >= el)
                    return true

                return false
            }
            //ทดสอบแบบก่อนเวลา




        } catch (e: Exception) {
            logger.error("Check time ERROR JOB:${job.name}: ${e.message}")
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

//    companion object {
        internal var logger = LoggerFactory.getLogger(CheckTimeService::class.java)
//    }
}