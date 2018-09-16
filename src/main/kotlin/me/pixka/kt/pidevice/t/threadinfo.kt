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

    @Scheduled(fixedDelay = 3000)
    fun checkThread() {
        var t = context.getBean("taskScheduler") as ThreadPoolTaskScheduler
        var tp = context.getBean("pool") as ExecutorService
        var aa = context.getBean("aa") as ThreadPoolExecutor
        val queue = t.scheduledThreadPoolExecutor
        val s = queue.queue

        logger.info("#threadinfo SS A: ${t.activeCount} PS: ${t.poolSize}  QS:${s.size} " +
                " C:${s.remainingCapacity()} CP:${queue.completedTaskCount}")
        var tt = tp as ThreadPoolExecutor
        logger.info("#threadinfo TT A: ${tt.activeCount} PS: ${tt.poolSize}  QS: ${tt.queue.size} " +
                " PSM : ${tt.corePoolSize}  ${tt.maximumPoolSize} C: ${tt.completedTaskCount}")
        logger.info("#threadinfo AA A: ${aa.activeCount} PS: ${aa.poolSize}  QS:${aa.queue.size} " +
                "QMS: ${aa.maximumPoolSize}  CP:${aa.completedTaskCount}")

        logger.info("#threadinfo ----------------------------------------------------------------")




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