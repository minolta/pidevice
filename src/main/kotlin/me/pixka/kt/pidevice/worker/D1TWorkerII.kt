package me.pixka.kt.pidevice.worker

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class D1TWorkerII(job: Pijob, var mtp: MactoipService, var readTmpService: ReadTmpService) : DWK(job), Runnable {

    var maxruntime = 0
    var maxwaittime = 0
    override fun run() {
        startRun = Date()
        isRun = true
        try {

                setPort()
                exitdate = findExitdate(pijob)
                status = "Job end ok."


        } catch (e: Exception) {
            isRun = false
            logger.error(e.message)
            status = "Error ${e.message}"
            mtp.lgs.createERROR("${e.message}", Date(),
                    "D1TWorkerII", Thread.currentThread().name, "17", "run()",
                    pijob.desdevice?.mac, pijob.refid)

        }
    }

    fun setPort() {
        try {
            var ports = mtp.getPortstatus(pijob)
            if (ports != null) {
                ports.forEach {
                    status = "Set port ${it.portname?.name} to ${it.status}"
                    mtp.setport(it)
                    if (it.runtime != null && it.runtime!!.toInt() > maxruntime)
                        maxruntime = it.runtime!!.toInt()

                    if (it.waittime != null && it.waittime!!.toInt() > maxwaittime)
                        maxwaittime = it.waittime!!.toInt()
                }

            }
        } catch (e: Exception) {
            status = "ERROR ${e.message}"
            logger.error(e.message)
            mtp.lgs.createERROR("${e.message}", Date(),
                    "D1TWorkerII", Thread.currentThread().name, "33", "setPort()",
                    pijob.desdevice?.mac, pijob.refid)
            throw e
        }
    }




    companion object {
        internal var logger = LoggerFactory.getLogger(D1TWorkerII::class.java)
    }
}