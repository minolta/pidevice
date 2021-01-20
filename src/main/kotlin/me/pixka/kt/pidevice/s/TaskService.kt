package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.collections.ArrayList

/**
 * ใช้สำหรับ run task ต่างๆ
 */
@Service
class TaskService(val context: ApplicationContext, val cts: CheckTimeService) {
    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่

    val pool = context.getBean("pool") as ExecutorService
    fun run(work: PijobrunInterface): Boolean {
        try {
            var forrun = checkalreadyrun(work)
            logger.debug("CheckJOB job can run ? ${forrun}")
            if (!forrun) {
                runinglist.add(work)
                logger.debug("CheckJOB Run this JOB: ${work.getPijobid()}")
                pool.submit(work as Runnable)
                logger.debug("Run ${work.getPijobid()} Buffer size ${runinglist.size}")
                return true
            } else {
                //มี job นี้ run อยู่แล้ว
                logger.warn("Have This job run already ${forrun}")
                return false
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false
    }

    /**
     *
     * สำหรับตรวจสอบว่า มี การ run อยู่เปล่า
     * return false ถ้า run อยู่
     * return true ถ้าไม่มีการ run อยู่
     */

    fun checkrun(w: Pijob): Boolean {
        try {
            if (runinglist.size < 1)
                return false //ถ้าไม่มี job เลย ส่ง
            var run = runinglist.find {
                it.getPijobid() == w.id && it.runStatus()
            }
            if (run != null)
                return true
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("${e.message}")
        }
        return true //ถ้า ERROR ก็ส่ง 1 ออกไปเลย
    }

    /**
     * สำหรับตรวจว่า job ไหน ยัง run ไม่เสร็จก็ไม่ต้อง run ทับละ
     * return true ถ้าเจอ return false ถ้าไม่เจอ
     */
    fun checkalreadyrun(w: PijobrunInterface): Boolean {
        try {
            logger.debug("CheckJOB runing size: ${runinglist.size} Job id: ${w.getPijobid()} REFID: ${w}")
            if (runinglist.size > 0) {
                logger.debug("CheckJOB have thread run ${runinglist.size}")
                if (runinglist.find { it.getPijobid() == w.getPijobid() && it.runStatus() } != null)
                    return true


                logger.debug("CheckJOB This jobcanrun ${w}")
                logger.debug("#Runjob TASKSERVICE  ${w.getPJ().name} ")
                return false //ถ้าไม่เจอ return w ไป exec
            } else {
                logger.debug("CheckJOB This jobcanrun ${w}")
                return false
            }

        } catch (e: Exception) {
            logger.error("Error check run ${e.message} ${w.getPJ()}")
        }
        return false
    }

    @Scheduled(fixedDelay = 1000)
    fun checkExitdate() {
        try {
            var now = Date().time
            runinglist.forEach {
                if (it.exitdate() != null && it.exitdate()?.time!! <= now) {
                    it.setrun(false) //end this job have to remove
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR set exit time ${e.message}")
        }

    }

    fun findExitdate(pijob: Pijob): Date? {
        var tvalue: Long? = 0L
        if (pijob.waittime != null)
            tvalue = pijob.waittime!!
        if (tvalue != null) {
            if (pijob.runtime != null)
                tvalue += pijob.runtime!!
        }
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, tvalue!!.toInt())
        val exitdate = calendar.time
        if (tvalue == 0L)
            return null

        return exitdate
    }

    @Scheduled(
        initialDelay = 120000,
        fixedDelay = 120000
    )
    fun removeEndjob(): List<PijobrunInterface> {
        var notrun = runinglist.filter { it.runStatus() == false }
        runinglist.removeAll(notrun)
        return runinglist
    }

    var df = SimpleDateFormat("HH:mm")

    /**
     * ตรวจสอบว่าอยู่ในช่วงเวลาหรือเปล่า
     * ใช้ checktimeservice
     */
    fun checktime(job: Pijob) = cts.checkTime(job, Date())


//    fun checktime(job: Pijob): Boolean {
//        var can = false
//        try {
//            var n = df.format(Date())
//            var now = df.parse(n)
//            logger.debug("checktime  ${job.name} : N:${n} now ${now} now time ${now.time}")
//            logger.debug("checktime  ${job.name} : s: ${job.stimes} ${now} e:${job.etimes}")
//            if (job.stimes != null && job.etimes != null) {
//                var st = df.parse(job.stimes).time
//                var et = df.parse(job.etimes).time
//                if (st <= now.time && now.time <= et)
//                    can = true
//                logger.debug("checktime  ${job.name} : ${st} <= ${now} <= ${et} can:${can}")
//
//            } else if (job.stimes != null && job.etimes == null) {
//                var st = df.parse(job.stimes).time
//                if (st <= now.time)
//                    can = true
//                logger.debug("checktime ${job.name} : ${st} <= ${now} Can:${can}")
//            } else if (job.stimes == null && job.etimes != null) {
//                var st = df.parse(job.etimes).time
//                if (st <= now.time)
//                    can = true
//                logger.debug("checktime ${job.name} : ${st} >= ${now} can:${can}")
//            } else {
//                logger.debug("${job.name} checktime not set ")
//                can = true
//            }
//        } catch (e: Exception) {
//            logger.error("checktime  ${job.name} : ${e.message}")
//        }
//
//        return can
//    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskService::class.java)
    }
}