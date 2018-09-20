package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pidevice.s.TaskService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor


@Component
@Profile("pi")
class ThreadInfo(val context: ApplicationContext, val tsk: TaskService, val ms: MessageService) {

    @Scheduled(fixedDelay = 15000)
    fun checkThread() {
      /*
        var t = context.getBean("taskScheduler") as ThreadPoolTaskScheduler
        var tp = context.getBean("pool") as ExecutorService
        var aa = context.getBean("aa") as ThreadPoolTaskExecutor
        var pt = context.getBean("pt") as ThreadPoolExecutor
        val queue = t.scheduledThreadPoolExecutor
        val s = queue.queue

        var aq = aa.threadPoolExecutor
        var f = aq.threadFactory

        logger.info("#threadinfo SS A: ${t.activeCount} PS: ${t.poolSize}  QS:${s.size} " +
                " C:${s.remainingCapacity()} CP:${queue.completedTaskCount}")
        var tt = tp as ThreadPoolExecutor
        logger.info("#threadinfo TT A: ${tt.activeCount} PS: ${tt.poolSize}  QS: ${tt.queue.size} " +
                " PSM : ${tt.corePoolSize}  ${tt.maximumPoolSize} C: ${tt.completedTaskCount}")
        logger.info("#threadinfo AA A: ${aa.activeCount} PS: ${aa.poolSize}  QS:${aq.queue.size} " +
                "QMS: ${aq.maximumPoolSize}  CP:${aq.completedTaskCount}")

        logger.info("#threadinfo PT A: ${pt.activeCount} PS: ${pt.poolSize}  QS:${pt.queue.size} " +
                "QMS: ${pt.maximumPoolSize}  CP:${pt.completedTaskCount}")
        logger.info("#threadinfo ----------------------------------------------------------------")

        val threadSet = Thread.getAllStackTraces().keys
        logger.debug("=======================================================")
        for (thread in threadSet) {
            logger.debug("threadlist : ===> ${thread.name} ${thread}")
        }
        logger.debug("=======================================================")

        /*
         var o = s.iterator()
         for(run in o)
         {
             logger.info("run : ${run}")
         }*/
         */
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ThreadInfo::class.java)
    }
}