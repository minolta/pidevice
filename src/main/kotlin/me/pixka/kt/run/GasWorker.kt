package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * จะ run ยาวเลยไม่หยุด แต่ถ้าอยู่น้อง
 */
class GasWorker(p: Pijob, gpios: GpioService, readUtil: ReadUtil, ps: PortstatusinjobService)
    : DefaultWorker(p, gpios, readUtil, ps, logger) {
    override fun run() {

        isRun = true
        startRun = Date()
        status = "Start job ${startRun}"
        while (true) {
            var ports = loadPorts(pijob)
            if (readUtil.checktmp(pijob)) {
                if (ports != null) {
                    setport(ports)

                    if (pijob.runtime != null) {
                        status = "Run time ${pijob.runtime}"
                        TimeUnit.SECONDS.sleep(pijob.runtime!!)
                    }
                } else {
                    logger.debug("Not port to set")
                    status = "No ports to set"
                }
            } else {
                if (ports != null) {
                    logger.debug("Reset port")
                    status = "reset port"
                    resetport(ports)
                    if (pijob.waittime != null) {
                        status = "Wait time ${pijob.waittime}"
                        TimeUnit.SECONDS.sleep(pijob.waittime!!)
                    }
                }
                status = "End job tmp out of range"
                isRun = false
                break
            }
        }

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(GasWorker::class.java)
    }
}
