package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.o.DistanceObj
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*


class DistanceWorker(job: Pijob, var mtp: MactoipService,
                     var portstatusinjobService: PortstatusinjobService) : DWK(job), Runnable {

    fun checkdistance(currentdistance: Double): Boolean {
        try {
            var dl = pijob.tlow?.toDouble()
            var dh = pijob.thigh?.toDouble()

            if (dl!! <= currentdistance && currentdistance <= dh!!)
                return true

            return false
        } catch (e: Exception) {
            mtp.lgs.createERROR("${e.message}", Date(),
                    "DistanceWorker", Thread.currentThread().name, "14",
                    "checkdistance()",
                    pijob.desdevice?.mac, pijob.refid, pijob.pidevice_id)
            throw  e
        }
    }

    override fun run() {

        isRun = true
        startRun = Date()

        try {
            var mac = pijob.desdevice?.mac
            var ip = mtp.mactoip(mac!!)

            if (ip != null) {
                var re = mtp.http.get("http://${ip}", 20000)
                var distance = mtp.om.readValue<DistanceObj>(re)
                if (distance.distance != null) {
                    if (checkdistance(distance.distance!!.toDouble())) {

                        go() //run
                        status = "Run complate "
                        exitdate = findExitdate(pijob)
                    }
                }
            }
        } catch (e: Exception) {
            mtp.lgs.createERROR("${e.message}", Date(),
                    "DistanceWorker", Thread.currentThread().name,
                    "37", "run()",
                    pijob.desdevice?.mac, pijob.refid, pijob.pidevice_id)
            logger.error(e.message)

        }


    }

    fun go() {
        var ports = portstatusinjobService.findByPijobid(pijob.id) as List<Portstatusinjob>
        if (ports != null) {
            ports.forEach {

                try {

                    var re = mtp.setport(it)
                    status  = "Set port ok"
                } catch (e: Exception) {
                    mtp.lgs.createERROR("${e.message}", Date(),
                            "DistanceWorker", Thread.currentThread().name,
                            "65", "go()",
                            it.device?.mac, pijob.refid, pijob.pidevice_id)
                    logger.error(e.message)
                }
            }

            status = "End set port"
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DistanceWorker::class.java)
    }
}