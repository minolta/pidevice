package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture


class OpenpumpsWorker(
    p: Pijob, var mtp: MactoipService,
    var target: String, var notify: NotifyService
) : DWK(p), Runnable {
    override fun run() {

        isRun = true
        startRun = Date()
        status = "Start run ${startRun}"
        var ports = mtp.getPortstatus(pijob)

        if (ports != null) {
            ports.forEach {
                //?delay=${timetoopen}
                CompletableFuture.supplyAsync {
                    var re = ""
                    var timetoopen = 10
                    try {
                        timetoopen  = it.runtime!!
                        var url = "http://${it.device?.ip}/run?delay=${timetoopen}"
                        re = mtp.http.get(url, 2000)
                    }catch (e:Exception)
                    {
                        logger.error("Open pumps Error ${it.device?.name} ${timetoopen}")
                        throw e
                    }
                    " Run ok OPEN PUMP : ${it.device?.name} ${re} RUN ${timetoopen}"
                }.thenAccept {
                    status = ""
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
    }

    var logger = LoggerFactory.getLogger(OpenpumpsWorker::class.java)


}