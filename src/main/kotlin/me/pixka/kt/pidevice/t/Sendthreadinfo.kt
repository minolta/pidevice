package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Threadinfo
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pidevice.s.TaskService
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor


@Component
class Sendthreadinfo(val context: ApplicationContext, val tsk: TaskService, val ms: MessageService,
                     val io: Piio, val http: HttpControl) {

    @Scheduled(fixedDelay = 15000)
    fun run() {
        var t = context.getBean("taskScheduler") as ThreadPoolTaskScheduler
        var tp = context.getBean("pool") as ExecutorService
        var aa = context.getBean("aa") as ThreadPoolExecutor


        val mapper = ObjectMapper()

        var runs = mapper.writeValueAsString(tsk.runinglist)

        var ti = Threadinfo()
        ti.info = runs
        var pi = PiDevice()
        ti.pidevice = pi
        var url = "http://endpoint.pixka.me:5001/addthreadinfo"

        http.postJson(url, ti)


    }
}