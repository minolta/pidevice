package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*


class RestartDeviceWorker(p: Pijob, val ip: String, val http: HttpService, val lgs: LogService) : DWK(p), Runnable {
    override fun run() {
        startRun = Date()
        isRun = true
        try {
            status = "restart ${ip}"
            var re = http.get("http://${ip}/restart", 120000)
            status = "restart ${re}"
            exitdate = findExitdate(pijob)
        } catch (e: Exception) {
            logger.error("JOB ${pijob.name} ERROR Reset device  ${e.message}")
            lgs.createERROR("JOB: ${pijob.name} ERROR ${e.message}", Date(), "RestartDeviceWorker",
                    Thread.currentThread().name, "14", "run()")
            status = "ERROR ${e.message}"
            isRun=false
        }
    }
    var logger = LoggerFactory.getLogger(RestartDeviceWorker::class.java)
}