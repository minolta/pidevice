package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import java.util.*


class RestartDeviceWorker(p: Pijob, val ip: String, val http: HttpService, val lgs: LogService) : DWK(p), Runnable {
    override fun run() {
        startRun = Date()
        isRun = true
        try {
            var re = http.get("http://${ip}/restart", 5000)
            status = "restart ${re}"
            exitdate = findExitdate(pijob)
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(), "RestartDeviceWorker",
                    "", "14", "run()")
            status = "ERROR ${e.message}"
            isRun=false
        }
    }

}