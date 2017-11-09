package me.pixka.kt.pidevice.s

import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
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
        logger.debug("Running size : ${tp.activeCount}  Job in buffer [${runinglist.size}] ")
        logger.debug("Jobin ${runinglist}")

    }


    /**
     * สำหรับตรวจว่า job ไหน ยัง run ไม่เสร็จก็ไม่ต้อง run ทับละ
     */
    fun checkalreadyrun(w: PijobrunInterface): PijobrunInterface? {

        if (runinglist.size > 0) {
            for (b in runinglist) {
                logger.debug("Check Run status ${b}")
                if (b.runStatus()) {
                    if (b.getPijobid().equals(w.getPijobid())) {

                        logger.debug("New run id:${w.getPijobid()} Runing list ${b.getPijobid()}")
                        logger.debug("Reject run ${w}")
                        return null //ถ้าเจอเหมือน null
                    }
                }
            }
            logger.debug("This job can run ${w}")
            return w //ถ้าไม่เจอ return w ไป exec
        } else {
            return w
        }





        return null
    }

    @Scheduled(fixedDelay = 5000)
    fun removefinished() {
        logger.debug("Start Remove job finished size: ${runinglist.size}")
        try {
            if (runinglist != null && runinglist.size > 0) {

                var items = runinglist.iterator()

                while(items.hasNext())
                {

                    var item = items.next()
                    logger.debug("Remove Job in list ${item}")
                    if (item != null)
                        if (!item.runStatus()) {
                            items.remove()
                            logger.debug("Remove finished run ${item} ")
                        }
                }

                /*
                for (old in runinglist) {
                    logger.debug("Remove Job in list ${old}")
                    if (old != null)
                        if (!old.runStatus()) {
                            logger.debug("Remove finished run ${old} ")
                            runinglist.remove(old)
                        }
                }
                */
            }


        } catch (e: Exception) {
            logger.error("Remove Error ${e.message}")
            e.printStackTrace()
        }

        logger.debug("Remove Already run size: ${runinglist.size}  ${runinglist}")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskService::class.java)
    }
}