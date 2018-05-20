package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pidevice.s.TaskService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor


@Component
@Profile("pi")
class ThreadInfo(val context: ApplicationContext, val tsk: TaskService, val ms: MessageService) {

    @Scheduled(fixedDelay = 60000)
    fun checkThread() {
        var t = context.getBean("taskScheduler") as ThreadPoolTaskScheduler
        var tp = context.getBean("pool") as ExecutorService
        var aa = context.getBean("aa") as ThreadPoolExecutor
        val queue = t.scheduledThreadPoolExecutor
        val s = queue.queue

        logger.info("Scheduler active count: ${t.activeCount} pool size: ${t.poolSize}  Queue size:${s.size} ")
        var tt = tp as ThreadPoolExecutor

        logger.info("Pool TaskService run: ${tt.activeCount}  Queue size: ${tt.queue.size} Pool size: ${tt.poolSize} Pool max size : ${tt.corePoolSize} / ${tt.maximumPoolSize} Complete ${tt.completedTaskCount}")

        if (tt.activeCount > 0) {
            var mes = ArrayList<String>()
            for (run in tsk.runinglist) {
                logger.debug("Runs id : " + run.getPijobid().toString() + " " + run.runStatus())

                var pj = run.getPJ()
                var job = pj.job
                mes.add("ID ${run.getPJ().refid} ${run.getPJ()} status: ${run.runStatus()}")


            }

            ms.tojsonmessage(mes, "threadinfo")

            logger.info("Job in device ->${mes}")
        }

        logger.info("Asyn AT: ${aa.activeCount} pool size: ${aa.poolSize}  Queue size:${aa.queue}  c:${aa.completedTaskCount}")

        /*
         var o = s.iterator()
         for(run in o)
         {
             logger.info("run : ${run}")
         }*/
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ThreadInfo::class.java)
    }
}