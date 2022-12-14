package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class GroupRunService(val task: TaskService) {
    /**
     * ใช้สำหรับตรวจสอบว่ามร job ที่ทำงานอยู่หรือเปล่าถ้ามีไม่ทำงาน
     */
    fun canrun(job: PijobrunInterface): Boolean {
        var pijobid = job.getPijobid().toInt()
        var groupid = job.getPJ().pijobgroup_id
        var grouprun = task.runinglist
        for (r in grouprun) {
            if (r is D1hjobWorker) {
                logger.debug("checkgroup D1Hworker [${job.getPJ().name}] check to  r:[${r.getPJ().name}] Run ? ${r.isRun} wait ${r.waitstatus}")
                //ถ้ามี job นี้ กำลัง run อยู
                if (r.getPijobid().toInt() == pijobid && r.runStatus()) {
                    logger.debug(" ${job} checkgroup This job is run in group ")
                    return false
                }
                //ถ้าอยู่ในกลุ่มเดียวกัน แล้ว r.ยังทำงานอยู่หรือ wait status false
                if (r.pijob.pijobgroup_id?.toInt() == groupid?.toInt() && !r.waitstatus) {
                    logger.debug(" ${job.getPJ().name} checkgroup Some one in group run ")
                    return false
                }
            } else if (r is D1portjobWorker) {
                logger.debug("checkgroup D1Portworker [${job.getPJ().name}] check to  r:[${r.getPJ().name}] Run ? ${r.isRun} wait ${r.waitstatus}")
                //ถ้ามี job นี้ กำลัง run อยู
                if (r.getPijobid().toInt() == pijobid && r.runStatus()) {
                    logger.debug(" ${job} checkgroup This job is run in group ")
                    return false
                }
                //ถ้าอยู่ในกลุ่มเดียวกัน แล้ว r.ยังทำงานอยู่หรือ wait status false
                if (r.pijob.pijobgroup_id?.toInt() == groupid?.toInt() && !r.waitstatus) {
                    logger.debug(" ${job.getPJ().name} checkgroup Some one in group run ")
                    return false
                }
            }

        }

        //ถ้าไม่เจอ ทำงานได้
        logger.debug("${job} checkgroup Now this job can run ")
        return true
    }

    /**
     * ใ้สำหรับค้นหา pijob ที่ใช้น้ำอยู่ถ้าใช้จะ return true
     */
    fun findOtherUserwater(job: Pijob): Boolean {
        try {

            var found = false
            //หาว่า มี Job ไหนยังไม่หยุดใช้น้ำ
            task.runinglist.forEach {
                if (it is D1hjobWorker) {
                    if (it.getPJ().pijobgroup_id == job.pijobgroup_id && it.waitstatus == false)
                        found = true
                }
            }

            return found

        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false
    }

    /***
     * สำหรับตรวจสอบว่าในกลุ่มของ job นี้มีการ ใช้น้ำอยู่หรือเปล่า
     * return true ถ้าไม่มีการใช้น้ำ
     * return false ถ้ามีการใช้น้ำ
     */
    fun c(j: Pijob): Boolean {
        try {
            var samerun = task.checkrun(j)
            if (samerun) {
                logger.warn("This job already run  from JOB ${j.name}")
                return false //ไม่สามารถ run job นี้ได้
            }
            //หาว่าobject ตัวไหนจะยัง run อยู่แต่อยู่ใน status run
            if (findOtherUserwater(j)) {
                logger.warn("Some one use water ${j.name}")
                return false //ไม่สามารถ run job นี้ได้
            }
        } catch (e: Exception) {
            logger.error(e.message)
            return false
        }
        return true
    }

    var logger = LoggerFactory.getLogger(GroupRunService::class.java)
}





