package me.pixka.kt.pidevice.s

import me.pixka.kt.pidevice.t.FindJobforRunDS18value
import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors

/**
 * ใช้สำหรับ run task ต่างๆ
 */
@Service
class TaskService() {
    val executor = Executors.newFixedThreadPool(30)
    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่


    fun run(work: PijobrunInterface) {

        var forrun = checkalreadyrun(work)

        if (forrun != null) {
            runinglist.add(forrun)
            executor.execute(forrun as Runnable)
        }
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

    private fun removefinished() {

        if (runinglist!=null && runinglist.size > 0) {
            for (old in runinglist) {
                if (!old.runStatus()) {
                    FindJobforRunDS18value.logger.debug("Remove finished run ${old}")
                    runinglist.remove(old)
                }
            }
        }
        logger.debug("Already run size: ${runinglist.size}  ${runinglist}")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskService::class.java)
    }
}