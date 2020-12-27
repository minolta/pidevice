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

    var rt = 0.0
    var wt = 0.0
    fun checkdistance(currentdistance: Double): Boolean {
        try {
            var dl = pijob.tlow?.toDouble()
            var dh = pijob.thigh?.toDouble()

            if (dl!! <= currentdistance && currentdistance <= dh!!)
                return true

            return false
        } catch (e: Exception) {
            status = "Error ${e.message}  checkdistance"
            mtp.lgs.createERROR("${e.message}", Date(),
                    "DistanceWorker", Thread.currentThread().name, "14",
                    "checkdistance()",
                    pijob.desdevice?.mac, pijob.refid, pijob.pidevice_id)
            logger.error("${e.message}")

            throw e
        }
    }

    fun getData(ip:String): DistanceObj {
        var re = mtp.http.get("http://${ip}", 20000)
        var distance = mtp.om.readValue<DistanceObj>(re)
        return distance
    }

    override fun run() {

        isRun = true
        startRun = Date()
        status = "Start run ${startRun}"

        try {
            var mac = pijob.desdevice?.mac
            var ip = mtp.mactoip(mac!!)

            if (ip != null) {

                var distance = getData(ip)

                if (distance.distance != null) {
                    if (checkdistance(distance.distance!!.toDouble())) {
                        go() //run
                        status = "Run complate "
                        exitdate = findExitdate(pijob, (rt + wt).toLong())
                    }
                    else
                    {
                        status = "Not in range"
                        exitdate = Date()
                        isRun = false
                    }
                } else {
                    isRun = false
                    status = "Not in range"
                    exitdate = Date()
                }
            } else {
                isRun = false
                status = "No ip"
                mtp.lgs.createERROR("No ip", Date(),
                        "DistanceWorker", Thread.currentThread().name,
                        "61", "run()",
                        pijob.desdevice?.mac, pijob.refid, pijob.pidevice_id)
            }


        } catch (e: Exception) {
            isRun = false
            exitdate = Date()
            status = "Error ${e.message}"
            logger.error(e.message)
            mtp.lgs.createERROR("${e.message}", Date(),
                    "DistanceWorker", Thread.currentThread().name,
                    "43", "run()",
                    pijob.desdevice?.mac, pijob.refid, pijob.pidevice_id)
        }


//        status = "End job"


    }

    fun go() {
        var ports = portstatusinjobService.findByPijobid(pijob.id) as List<Portstatusinjob>
        if (ports != null) {
            ports.forEach {
                try {
                    status = "Set device ${it.device?.name} port ${it.portname?.name}"
                    var re = mtp.setport(it)
                    if (it.runtime != null && it.runtime!!.toDouble() > rt)
                        rt = it.runtime!!.toDouble()

                    if (it.waittime != null && it.waittime!!.toDouble() > wt)
                        wt = it.waittime!!.toDouble()
                    status = "Set port ok"
                } catch (e: Exception) {
                    status = "ERROR ${e.message}"
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


        internal var logger = LoggerFactory.getLogger(DistanceWorker::class.java)
}