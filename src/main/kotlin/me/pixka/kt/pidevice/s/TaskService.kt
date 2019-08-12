package me.pixka.kt.pidevice.s

import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * ใช้สำหรับ run task ต่างๆ
 */
@Service
//@Profile("pi", "lite")
class TaskService(val context: ApplicationContext) {
    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่
    fun run(work: PijobrunInterface): Boolean {
        try {
            val pool = context.getBean("pool") as ExecutorService
            var forrun = checkalreadyrun(work)
            logger.debug("CheckJOB job can run ? ${forrun}")
            if (forrun != null) {
                runinglist.add(forrun)
                logger.debug("CheckJOB Run this JOB: ${forrun.getPijobid()}")
                pool.submit(forrun as Runnable)
                logger.debug("Run ${forrun.getPijobid()} Buffer size ${runinglist.size}")
                return true
            } else {
                //มี job นี้ run อยู่แล้ว
                logger.error("Have This job run already ${forrun}")
                return false
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false
    }


    /**
     * สำหรับตรวจว่า job ไหน ยัง run ไม่เสร็จก็ไม่ต้อง run ทับละ
     */
    fun checkalreadyrun(w: PijobrunInterface): PijobrunInterface? {

        try {
            logger.debug("CheckJOB runing size: ${runinglist.size} Job id: ${w.getPijobid()} REFID: ${w}")
            if (runinglist.size > 0) {
                logger.debug("CheckJOB have thread run ${runinglist.size}")
                for (b in runinglist) {
                    try {
                        logger.debug("CheckJOB runstatus ${b.runStatus()} id: ${w.getPijobid()}")
                        logger.debug("CheckJOB ${b.getPijobid()} == ${w.getPijobid()}")
                        if (b.getPijobid().toInt() == w.getPijobid().toInt()) {
                            if (b.runStatus()) {
                                logger.error("CheckJOB Reject run ${w}")
                                return null //ถ้าเจอเหมือน null
                            }
                        }
                        logger.debug("CheckJOB Next Check")
                    } catch (e: Exception) {
                        logger.error("Check run error ${e.message}")
                    }
                }

                logger.debug("CheckJOB This jobcanrun ${w}")
                return w //ถ้าไม่เจอ return w ไป exec
            } else {
                logger.debug("CheckJOB This jobcanrun ${w}")
                return w
            }

        } catch (e: Exception) {
            logger.error("Error check run ${e.message}")
        }




        return null
    }


    fun runAsyn(f: Future<Any>, timeout: Int): Any? {
        try {
            var count = 0
            while (true) {
                if (f.isDone) {
                    logger.info("Run commplete")
                    return f.get()
                    break
                }
                TimeUnit.SECONDS.sleep(1)
                count++

                if (count > timeout) {
                    f.cancel(true)
                    logger.error("Timeout")
                    return null

                }

            }

        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    @Scheduled(initialDelay = 2000,
            fixedDelay = 20000)
    fun removefinished() {
        logger.debug("Start Remove job finished size: ${runinglist.size}")
        try {
            if (runinglist != null && runinglist.size > 0) {

                var items = runinglist.iterator()
                logger.debug("Size Before remove ${runinglist.size}")
                while (items.hasNext()) {

                    var item = items.next()
                    logger.debug("Remove Job in list ${item}")
                    if (item != null)
                        if (!item.runStatus()) {
                            items.remove()
                            logger.debug("Remove finished run ${item} ")
                        }
                }

                logger.debug("Size after remove ${runinglist.size}")

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

        logger.debug("Remove Already run size: ${runinglist.size}    ")

        logger.debug(" THREADISRUN ======================================RUN===========================================  ")
        for (run in runinglist) {
            logger.debug("THREADISRUN ID:${run.getPJ().name} ${run.getPijobid()} Start date ${run.startRun()} Status :${run.state()} " +
                    "is run ? : ${run.runStatus()}")
        }
        logger.debug("THREADISRUN ======================================================================================")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskService::class.java)
    }
}