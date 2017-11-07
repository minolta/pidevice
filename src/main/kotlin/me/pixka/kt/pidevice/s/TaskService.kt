package me.pixka.kt.pidevice.s

import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

/**
 * ใช้สำหรับ run task ต่างๆ
 */
@Service
class TaskService() {
    val executor = Executors.newFixedThreadPool(50)
    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่


    fun run(work: PijobrunInterface) {

        var forrun = checkalreadyrun(work)

        if (forrun != null) {
            runinglist.add(forrun)
            executor.execute(forrun as Runnable)
            logger.debug("Run ${forrun.getPijobid()}")
        }
        var tp = executor as ThreadPoolExecutor
        logger.debug("Running size : ${tp.activeCount} Job in ${runinglist}")
    }


    /**
     * สำหรับตรวจว่า job ไหน ยัง run ไม่เสร็จก็ไม่ต้อง run ทับละ
     */
    fun checkalreadyrun(w: PijobrunInterface): PijobrunInterface? {
        removefinished()
        if (runinglist.size > 0) {
            for (b in runinglist) {

                if (b.getPijobid().equals(w.getPijobid())) {

                    logger.debug("New run id:${w.getPijobid()} Runing list ${b.getPijobid()}")
                    logger.debug("Reject run ${w}")
                    //runs.remove(forrun) //เอาออกไม่ต้อง run pijob ตัวนี้เพราะยังทำงานไม่เสร็จรอรอบหน้า
                    return null //ถ้าเจอเหมือน null
                }
            }
            return w //ถ้าไม่เจอ return w ไป exec
        } else {
            return w
        }





        return null
    }

    fun removefinished() {

        try {
            if (runinglist != null && runinglist.size > 0) {
                for (old in runinglist) {
                    if (!old.runStatus()) {
                        logger.debug("Remove finished run ${old}")
                        runinglist.remove(old)
                    }
                }
            }
            logger.debug("Already run size: ${runinglist.size}  ${runinglist}")
        } catch (e: Exception) {
            logger.error("Remove Error ${e.message}")
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskService::class.java)
    }
}