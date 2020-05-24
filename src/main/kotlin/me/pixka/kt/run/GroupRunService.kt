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
//        logger.debug("Groups ${grouprun}")
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

    /***
     * สำหรับตรวจสอบว่าในกลุ่มของ job นี้มีการ ใช้น้ำอยู่หรือเปล่า
     * return true ถ้าไม่มีการใช้น้ำ
     * return false ถ้ามีการใช้น้ำ
     */
    fun c(j: Pijob): Boolean {

        var pijobid = j.id
        var groupid = j.pijobgroup_id
        var grouprun = task.runinglist


        var samerun = grouprun.find {
            it is D1hjobWorker && (it.getPijobid().toInt() == j.id.toInt() && it.runStatus())
        }

        if (samerun != null) {
            logger.error("This job already run  from JOB ${j.name}")
            return false //ไม่สามารถ run job นี้ได้
        }

        //หาว่าobject ตัวไหนจะยัง run อยู่แต่อยู่ใน status run
        var someoneusewater = grouprun.find {
            it is D1hjobWorker &&
                    it.pijob.pijobgroup_id?.toInt() == groupid?.toInt() && !it.waitstatus
                    && it.runStatus()
        }
        if(someoneusewater!=null) {
            logger.error("Some one use water ${j.name}")
            return false //ไม่สามารถ run job นี้ได้
        }
        return true


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(GroupRunService::class.java)
    }
}





