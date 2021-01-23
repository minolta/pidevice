package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.pidevice.worker.D1TimerII
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class runD1Timer(
    val pjs: PijobService,
    val js: JobService, val findJob: FindJob,
    val task: TaskService, val ips: IptableServicekt,
    val line: NotifyService, val mtps: MactoipService,
    val dhs: Dhtutil, val httpService: HttpService, val psij: PortstatusinjobService,
    val readUtil: ReadUtil
) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {

        try {
            var list = findJob.loadjob("runtimerbyd1")

            if (list != null) {
                list.forEach {

                    if (!task.checkrun(it)) {
                        try {
//                            var tc = mtps.readTmp(it)?.toDouble()
//                            var tt = it.tlow?.toDouble()

                            var t = D1TimerII(it, mtps, line)
                            if (task.run(t)) {

                            } else {

                            }
                        }catch (e:Exception)
                        {}

                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }


var logger = LoggerFactory.getLogger(runD1Timer::class.java)
}