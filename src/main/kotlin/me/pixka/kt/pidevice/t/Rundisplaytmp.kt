package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.DisplaytmpWorker
import me.pixka.log.d.LogService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Rundisplaytmp(val lgs: LogService, val findJob: FindJob,val httpService: HttpService,
                    val task: TaskService, val iptableServicekt: IptableServicekt,
                    val portstatusinjobService: PortstatusinjobService,val ct:CheckTimeService) {

    @Scheduled(fixedDelay = 1000)
    fun run() {
        var mac: String? = null
        var jobid: Long? = null
        var ownid: Long? = null
        try {
            var jobs = findJob.loadjob("displaytmp")

            if (jobs != null) {
                jobs.forEach {
                    mac = it.desdevice?.mac
                    jobid = it.refid
                    ownid = it.pidevice_id
                    val ports = portstatusinjobService.findByPijobid(it.id)
                    if (!task.checkrun(it)) {
                        task.run(DisplaytmpWorker(it, lgs,httpService,iptableServicekt,
                                ports as List<Portstatusinjob>?,ct))
                    }
                }
            }

        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(), "Rundisplaytmp",
                    "", "18", "run", mac, jobid, ownid)
        }

    }

}