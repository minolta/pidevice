package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.t.RungasJob
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * จะ run ยาวเลยไม่หยุด แต่ถ้าอยู่น้อง
 */
class GasWorker(p: Pijob, gpios: GpioService, readUtil: ReadUtil, ps: PortstatusinjobService, var runwithjob: Pijob)
    : DefaultWorker(p, gpios, readUtil, ps, logger) {
    override fun run() {
        var ports: List<Portstatusinjob>? = null
        try {
            isRun = true
            startRun = Date()
            status = "Start job ${startRun}"

            ports = loadPorts(pijob)

            while (true) {

                if (readUtil.checktmp(pijob) && runwith(runwithjob)) {
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
                        rt(ports)
                    }
                    status = "End job tmp out of range"
                    logger.error("End job tmp out of range")
                    isRun = false
                    break
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            isRun = false
            status = e.message
            if (ports != null)
                rt(ports)


        }
    }

    fun rt(ports: List<Portstatusinjob>) {
        try {
            logger.debug("Reset port")
            status = "reset port"
            resetport(ports)
            if (pijob.waittime != null) {
                status = "Wait time ${pijob.waittime}"
                TimeUnit.SECONDS.sleep(pijob.waittime!!)
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    //สำหรับ test job ที่ run ว่า ok มันทำงานได้เปล่าถ้าใช่ก็ run ต่อไปได้
    fun runwith(testjob: Pijob?): Boolean {
        RungasJob.logger.debug("Start runwidth")
        try {
            //ถ้ามี runwidthid
            logger.debug("runwidth Check job ${testjob}")
            if (testjob != null) {
                var re = readUtil.checktmp(testjob)
                logger.debug("runwidth Test result ${re}")
                return re
            }
        } catch (e: Exception) {
            logger.error("Error runwith() ${e.message}")
            throw  e
        }
        return false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(GasWorker::class.java)
    }
}
