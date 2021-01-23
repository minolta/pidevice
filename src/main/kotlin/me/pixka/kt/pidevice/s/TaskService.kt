package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import kotlin.collections.ArrayList

/**
 * ใช้สำหรับ run task ต่างๆ
 */
@Service
class TaskService(val context: ApplicationContext, val cts: CheckTimeService) {
//    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่
    var runinglist = CopyOnWriteArrayList<PijobrunInterface>()
    var removeStatus = false //สำหรับบอกว่าเอา job ที่ run เสร็จแล้วออกก่อน
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
            logger.error("TaskService ${e.message}")
        }
        return false
    }

    /**
     *
     * สำหรับตรวจสอบว่า มี การ run อยู่เปล่า
     * return true ถ้า run อยู่
     * return false ถ้าไม่มีการ run อยู่
     */

    fun checkrun(w: Pijob): Boolean {

        var items = runinglist.iterator()

        while(items.hasNext())
        {
            var item = items.next()
            if( item.getPijobid() == w.id && item.runStatus())
            {
                    return true
            }

        }

        return false
    }

    /**
     * สำหรับตรวจว่า job ไหน ยัง run ไม่เสร็จก็ไม่ต้อง run ทับละ
     * return true ถ้าเจอ return false ถ้าไม่เจอ
     */
    fun checkalreadyrun(w: PijobrunInterface): Boolean {

        try {
            var items = runinglist.iterator()

            while (items.hasNext()) {

                var item = items.next()
//                logger.error("Check run ${item.getPJ().name}")
                if (item.getPijobid() == w.getPijobid() && item.runStatus()) {
//                    logger.error("Found job run ${item.getPJ().name}")
                    return true
                }
            }
            return false
        }
        catch (e:Exception)
        {
            logger.error("ERROR Check already run ${e.message}")
        }
        return true
    }

    @Scheduled(fixedDelay = 1000)
    fun checkExitdate() {
        try {
            var now = Date().time
            var items =  runinglist.iterator()
            items.forEach {
                if (it.exitdate() != null && it.exitdate()?.time!! <= now) {
                    it.setrun(false) //end this job have to remove
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR set exit time ${e.message}")
        }

    }


    @Scheduled(
        initialDelay = 1000,
        fixedDelay = 1000
    )
    fun removeEndjob(): List<PijobrunInterface> {
        var items = runinglist.iterator()
        while(items.hasNext())
        {
            var item = items.next()
            if(!item.runStatus())
                runinglist.remove(item)
        }
        return runinglist
    }

    var df = SimpleDateFormat("HH:mm")

    /**
     * ตรวจสอบว่าอยู่ในช่วงเวลาหรือเปล่า
     * ใช้ checktimeservice
     */
    fun checktime(job: Pijob) = cts.checkTime(job, Date())


    var logger = LoggerFactory.getLogger(TaskService::class.java)
}