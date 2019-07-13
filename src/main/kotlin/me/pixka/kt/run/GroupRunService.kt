package me.pixka.kt.run

import me.pixka.kt.pidevice.s.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class GroupRunService(val task: TaskService) {

    fun canrun(d1hjob: D1hjobWorker): Boolean {
        var pijobid = d1hjob.pijob.id.toInt()
        var groupid = d1hjob.pijob.pijobgroup_id
        var grouprun = task.runinglist
//        logger.debug("Groups ${grouprun}")
        for (r in grouprun) {

            if (r is D1hjobWorker) {
                logger.debug("checkgroup D1Hworker [${d1hjob.getPJ().name}] check to  r:[${r.getPJ().name}] Run ? ${r.isRun} wait ${r.waitstatus}")
                //ถ้ามี job นี้ กำลัง run อยู
                if (r.getPijobid().toInt() == pijobid && r.runStatus()) {
                    logger.debug(" ${d1hjob} checkgroup This job is run in group ")
                    return false
                }
                //ถ้าอยู่ในกลุ่มเดียวกัน แล้ว r.ยังทำงานอยู่หรือ wait status false
                if (r.pijob.pijobgroup_id?.toInt() == groupid?.toInt() && !r.waitstatus) {
                    logger.debug(" ${d1hjob.getPJ().name} checkgroup Some one in group run ")
                    return false
                }
            }

        }

        //ถ้าไม่เจอ ทำงานได้
        logger.debug("${d1hjob} checkgroup Now this job can run ")
        return true
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(GroupRunService::class.java)
    }
}




