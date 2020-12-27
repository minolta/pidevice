package me.pixka.kt.pidevice.worker

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class DisplaydhtWorker(pijob: Pijob, var mtp: MactoipService, var ports: List<*>?) : DWK(pijob), Runnable {
    override fun run() {
        isRun = true
        startRun = Date()
        try {
            var dhtip = mtp.mactoip(pijob.desdevice?.mac!!)
            var dht = mtp.dhts.iptodhtobj(dhtip!!)

            if (dht != null) {

                var ps = ports as List<Portstatusinjob>

                ps.forEach {
                    try {
                        var name = pijob.desdevice?.name
                        var ip = mtp.mactoip(it.device?.mac!!)
                        var url = mtp.http.encode("ค่าความชืนของ ${name} = ${dht.h} อุณหภูมิ ${dht.t} ")
                        var re = mtp.http.get("http://${ip}/settext?t=${url}&tn=2&l=4",5000)
                        status = "http://${ip}/settext?t=${url}&tn=2&l=4"
                    } catch (e: Exception) {
                        mtp.lgs.createERROR("${e.message}", Date(),
                                "DisplaydhtWorker", Thread.currentThread().name, "24", "run()",
                                "${it.device?.mac}", pijob.refid)
                        logger.error(e.message)
                    }
                }
            }

            exitdate = findExitdate(pijob)
            status = "End job wait exitdate"

        } catch (e: Exception) {
            mtp.lgs.createERROR("${e.message}",Date(),
            "DisplaydhtWorker",Thread.currentThread().name,"16","run()",pijob.pidevice?.mac)
            logger.error(e.message)
            isRun=false
            status ="Error ${e.message}"

        }


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DisplaydhtWorker::class.java)
    }
}
