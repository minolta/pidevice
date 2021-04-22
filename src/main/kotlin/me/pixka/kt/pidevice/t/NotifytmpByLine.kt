package me.pixka.kt.pidevice.t

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.ReadStatusService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.NotifytmpWorker
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


/**
 * ใช้สำหรับ job ที่บอกว่า pressure อยู่ในช่วงที่กำหนดหรือเปล่า
 */
@Component
@Profile("!test")
class NotifytmpByLine(val mac:MactoipService, val notifyService: NotifyService,
                      val findjob: FindJob, val iptableService: IptableServicekt,
                      val task: TaskService) {
    @Scheduled(fixedDelay = 1000)
    fun run() {
        try {
            var jobs = findjob.loadjob("notifytmp")
            if (jobs != null) {
                jobs.map {
                    if (it.desdevice != null) {
                        var ip = iptableService.findByMac(it.desdevice?.mac!!)
                        if (ip != null) {
                            var t = "http://${ip.ip}"
                            var n = NotifytmpWorker(it, mac, t, notifyService)
                            task.run(n)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(NotifyPressureByLine::class.java)
    }


}