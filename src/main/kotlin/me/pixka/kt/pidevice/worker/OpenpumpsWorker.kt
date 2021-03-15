package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.c.Statusobj
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture


class OpenpumpsWorker(
    p: Pijob, var mtp: MactoipService,
    var notify: NotifyService
) : DWK(p), Runnable {
    override fun run() {

        isRun = true
        startRun = Date()
        status = "Start run ${startRun}"
        var ports = mtp.getPortstatus(pijob)
//        mtp.findTimeofjob(pijob)
        if (ports != null) {
            ports.forEach {
                //?delay=${timetoopen}
                CompletableFuture.supplyAsync {
                    var re = ""
                    var timetoopen = 10
                    try {
                        timetoopen = it.runtime!!
                        var url = "http://${it.device?.ip}/run?delay=${timetoopen}"
                        status = "Open ${it.device?.name} in ${timetoopen}"
                        re = mtp.http.get(url, 2000)
                    } catch (e: Exception) {
                        logger.error("Open pumps Error ${it.device?.name} ${timetoopen}")
                        status = "Open pumps Error ${it.device?.name} ${timetoopen}"
                        throw e
                    }
                    var s = mtp.om.readValue<Statusobj>(re)
                    " Run ok OPEN PUMP : ${it.device?.name} Pump uptime : ${s.uptime} RUN ${timetoopen}"
                }.thenAccept {
                    status = it
                }.exceptionally {
                    logger.error("Error Job ${it.message}")
                    it.printStackTrace()
                    null

                }
//                try {
//                    var timetoopen = it.runtime
//                    var url = "http://${it.device?.ip}/run?delay=${timetoopen}"
//                    mtp.http.get(url,2000)
//
//                } catch (e: Exception) {
//                    logger.error("Open pumps ERROR ${e.message}")
//                }
            }
        }

        status = "End openpumps job"
        exitdate = findExitdate(pijob)

    }

    var logger = LoggerFactory.getLogger(OpenpumpsWorker::class.java)


}