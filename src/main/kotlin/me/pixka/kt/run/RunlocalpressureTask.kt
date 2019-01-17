package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class RunlocalpressureTask(p: Pijob, gpios: GpioService, readUtil: ReadUtil, ps: PortstatusinjobService)
    : DefaultWorker(p, gpios, readUtil, ps, GasWorker.logger) {
    var finishrun: Date? = null
    var timeout: Long? = null
    var run: Long? = null
    override fun run() {

        isRun = true
        startRun = Date()
        status = "Start job ${startRun}"
        logger.debug("Start job ${startRun}")
        var ports = loadPorts(pijob)
        getTime()
        var isSetport = false
        while (true) {


            if (readUtil.checkLocalPressure(pijob)) {


                if (ports != null) {
                    if (!isSetport) {
                        setport(ports)
                        isSetport = true
                    }
                    status = "Wait 1 sec"
                    TimeUnit.SECONDS.sleep(1)
//                    if (pijob.runtime != null) {
//                        status = "Run time ${pijob.runtime}"
//                        TimeUnit.SECONDS.sleep(1)
//                    }
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
                status = "End job pressure out of range"
                logger.debug("End job pressure out of range")
                isRun = false
                break
            }

            if (checkRuntime()) {
                if (ports != null) {
                    logger.debug("Reset port")
                    status = "reset port"
                    resetport(ports)
                    if (pijob.waittime != null) {
                        status = "Wait time ${pijob.waittime}"
                        TimeUnit.SECONDS.sleep(pijob.waittime!!)
                    }
                }
                status = "End job  Pressure timeout"
                logger.debug("End job pressure timeout")
                isRun = false
                break
            } else {
                status = "Not end time"
            }
        }

    }

    fun checkRuntime(): Boolean {
        var now = Date().time
        logger.debug("Check time NOW: ${now} >= ${finishrun!!.time}")
        if (now >= finishrun!!.time) {
            return true
        }
        return false
    }

    fun getTime() {
        try {
            if (startRun == null) {
                startRun = Date()
                timeout = pijob.waittime!! //
                run = pijob.runtime //เวลาในการ run เอ็นวินาที
                finishrun = DateTime().plusSeconds(pijob.runtime?.toInt()!!).toDate() //เวลาเสร็จ
                logger.debug("Counter info START : ${startRun} RUN TIME: ${finishrun}")
            }
        } catch (e: Exception) {
            CounterandrunWorker.logger.error("GETTIME: ${e.message}")
            status = "Error ${e.message}"
            isRun = false
            throw e
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(RunlocalpressureTask::class.java)
    }
}


