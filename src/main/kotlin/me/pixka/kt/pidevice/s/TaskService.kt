package me.pixka.kt.pidevice.s

import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * ใช้สำหรับ run task ต่างๆ
 */
@Service
class TaskService(val context: ApplicationContext) {
    //val executor = Executors.newFixedThreadPool(50)
    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่

    val queue = ThreadPoolExecutor(5, 10, 30,
    TimeUnit.MINUTES, LinkedBlockingDeque<Runnable>(50),
    ThreadPoolExecutor.CallerRunsPolicy())

    fun run(work: PijobrunInterface) {

        val pool = context.getBean("pool") as ExecutorService


        var forrun = checkalreadyrun(work)

        if (forrun != null) {
            runinglist.add(forrun)

            //pool.submit(forrun as Runnable)
            pool.submit(forrun as Runnable)
            /*
            var t = Thread(forrun as Runnable)
            t.start()
            */
            logger.debug("Run ${forrun.getPijobid()} Buffer size ${runinglist.size}")
        }
        /*
        var tp = threadpool as ThreadPoolExecutor
        logger.debug("Queue size :${tp.queue.size} Running size : ${tp.activeCount}  Job in buffer [${runinglist.size}] ")

        */
        logger.debug("Jobs in List  ${runinglist.size} ThreadInfo")

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

    @Scheduled(initialDelay = 2000,
            fixedDelay = 5000)
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

        logger.debug("Remove Already run size: ${runinglist.size}  ${runinglist}  ")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskService::class.java)
    }
}