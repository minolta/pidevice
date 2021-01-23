package me.pixka.kt.pidevice.worker

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class DisplaydhtWorker(pijob: Pijob, var mtp: MactoipService) : DWK(pijob), Runnable {
    override fun run() {
        isRun = true
        startRun = Date()
        try {
            var dhtip = mtp.mactoip(pijob.desdevice?.mac!!)
            var dht = mtp.dhts.iptodhtobj(dhtip!!)

            if (dht != null) {

                var ps = mtp.getPortstatus(pijob)
                ps?.forEach {
                    try {
                        var name = pijob.desdevice?.name
                        var ip = mtp.mactoip(it.device?.mac!!)
                        var url = mtp.http.encode("ค่าความชืนของ ${name}: ${dht.h} อุณหภูมิ: ${dht.t} ")
                        var re = mtp.http.get("http://${ip}/settext?t=${url}&tn=2&l=4", 60000)
                        status = "http://${ip}/settext?t=${url}&tn=2&l=4"
                    } catch (e: Exception) {
                        isRun=false
                        logger.error("Display ERROR :  ${e.message}")
                        status = "Display ERROR :  ${e.message}"
                        mtp.lgs.createERROR(
                            "${e.message}", Date(),
                            "DisplaydhtWorker", Thread.currentThread().name, "24", "run()",
                            "${it.device?.mac}", pijob.refid
                        )
                    }
                }
            }

            exitdate = findExitdate(pijob)
            status = "End job wait exitdate"

        } catch (e: Exception) {
            isRun = false
            mtp.lgs.createERROR(
                "${e.message}", Date(),
                "DisplaydhtWorker", Thread.currentThread().name, "16", "run()", pijob.pidevice?.mac
            )
            logger.error("JOB ${pijob.name} : DisplaydhtWorker ${e.message}")
            status = "Error ${e.message}"

        }


    }

    var logger = LoggerFactory.getLogger(DisplaydhtWorker::class.java)

}
