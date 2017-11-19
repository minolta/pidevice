package me.pixka.kt.pidevice.t

import me.pixka.kt.pidevice.s.TaskService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import sun.java2d.Disposer.getQueue
import java.util.concurrent.BlockingQueue



@Component
class ThreadInfo(val context: ApplicationContext,val tsk:TaskService) {

   // @Scheduled(fixedDelay = 10000)
    fun checkThread()
    {
       var t= context.getBean("taskScheduler") as ThreadPoolTaskScheduler

        logger.info("${t} active count ${t.activeCount} pool size${t.poolSize}")

        val queue = t.scheduledThreadPoolExecutor
        val s = queue.queue.iterator()
        while(s.hasNext())
        {
            logger.info("JOB in ${s.next()}")
        }


        logger.info("Task Service ${tsk} ")
        var pool = tsk.executor





    }
    companion object {
        internal var logger = LoggerFactory.getLogger(ThreadInfo::class.java)
    }
}