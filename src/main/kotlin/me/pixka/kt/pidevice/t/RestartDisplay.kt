package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.RestartDeviceWorker
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*


@Component
class RestartDevice(val http: HttpService, val findJob: FindJob, var task: TaskService, var lgs: LogService,
                    var iptableServicekt: IptableServicekt) {


    @Scheduled(fixedDelay = 5000)
    fun restartDipslay() {
        var jobs = findJob.loadjob("restartdevice")
        try {
            if (jobs != null) {
                jobs.forEach {

                    if (!task.checkrun(it)) {
                        var ip = iptableServicekt.findByMac(it.desdevice?.mac!!)
                        if (ip != null) {
                            task.run(RestartDeviceWorker(it, "${ip.ip}",http,lgs))

                        }else
                        {
                            lgs.createERROR("IP NOT FOUND",Date(),"RestartDevice",
                            "","29","restartDisplay()",it.desdevice?.mac,it.refid)
                            logger.error("Ip NOt found")
                        }
                    }

                }
            }
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(), "RestartDevice", "",
                    "22", "restartDisplay()", null, null)
            logger.error(e.message)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RestartDevice::class.java)
    }

}