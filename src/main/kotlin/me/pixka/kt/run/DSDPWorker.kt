package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.stereotype.Component
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.Comparator

@Profile("pi")
//@Component
class DSDPWorker(val ss: SensorService, val dps: DisplayService, var pijob: Pijob) : Runnable, PijobrunInterface {
    override fun state(): String? {
        return state
    }

    var state: String? = " Create "
    override fun startRun(): Date? {
        return startRun
    }

    var startRun: Date? = null

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    var isRun: Boolean = true

    override fun runStatus(): Boolean {
        return isRun
    }

    var gpio: GpioService? = null

    override fun setG(gpios: GpioService) {
        gpio = gpios
    }


    override fun setP(p: Pijob) {
        pijob = p
    }


    fun rs(): Boolean {
        logger.info("Run DSDPWork")
        isRun = true
        if (pijob?.desdevice_id != null && pijob.ds18sensor_id != null) {
            logger.debug(" JOB ${pijob}")
            var dsvalue = ss.readDsOther(pijob?.desdevice_id!!, pijob?.ds18sensor_id!!) //อ่านค่าตัวอื่น
            if (dsvalue != null) {
                var count = 0
                while (dps.lock) {
                    logger.debug("Wait for lock display")
                    TimeUnit.MILLISECONDS.sleep(200)
                    count++
                    if (count >= 20) {
                        logger.error("Lock display time out")
                        isRun = false
                        return false
                    }
                }
                dps.lockdisplay(this)
                var d = df.format(dsvalue.t)
                if (d.length > 4) {
                    d = "*" + d100.format(dsvalue.t)

                }
                dps.dot.print(d)
                dps.unlock(this)
                isRun = false
                logger.debug("End DSDPTASK")

            } else {
                logger.error("Read other fail ${pijob}")
                return false
            }
        }
        logger.debug("Dpsp Run is ok")
        return true
    }

    @Async("aa")
    fun runasync(): Future<Boolean>? {
        logger.info("Run DSDPWork")
        isRun = true
        if (pijob?.desdevice_id != null && pijob.ds18sensor_id != null) {
            logger.debug(" JOB ${pijob}")
            var dsvalue = ss.readDsOther(pijob?.desdevice_id!!, pijob?.ds18sensor_id!!) //อ่านค่าตัวอื่น
            if (dsvalue != null) {
                var count = 0
                while (dps.lock) {
                    logger.debug("Wait for lock display")
                    TimeUnit.MILLISECONDS.sleep(200)
                    count++
                    if (count >= 20) {
                        logger.error("Lock display time out")
                        isRun = false
                        return AsyncResult(false)
                    }
                }
                dps.lockdisplay(this)
                var d = df.format(dsvalue.t)
                if (d.length > 4) {
                    d = "*" + d100.format(dsvalue.t)

                }
                dps.dot.print(d)
                dps.unlock(this)
                isRun = false
                logger.debug("End DSDPTASK")

            } else {
                logger.error("Read other fail ${pijob}")
                return AsyncResult(false)
            }
        }
        logger.debug("Dpsp Run is ok")
        return AsyncResult(true)
    }

    var df = DecimalFormat("##.0")
    var d100 = DecimalFormat("###")
    override fun run() {

        try {

            logger.info("Run DSDPWork")
            isRun = true
            if (pijob?.desdevice_id != null && pijob.ds18sensor_id != null) {

                logger.debug(" JOB ${pijob}")
                var dsvalue = ss.readDsOther(pijob?.desdevice_id!!, pijob?.ds18sensor_id!!) //อ่านค่าตัวอื่น
                if (dsvalue != null) {
                    var count = 0
                    while (dps.lock) {
                        logger.debug("Wait for lock display")
                        TimeUnit.MILLISECONDS.sleep(200)
                        count++
                        if (count >= 20) {
                            logger.error("Lock display time out")
                            isRun = false
                            return
                        }
                    }
                    dps.lockdisplay(this)
                    var d = df.format(dsvalue.t)
                    if (d.length > 4) {
                        d = "*" + d100.format(dsvalue.t)

                    }
                    dps.dot.print(d)
                    dps.unlock(this)
                    isRun = false
                    logger.debug("End DSDPTASK")

                } else {
                    logger.error("Read other fail ${pijob}")
                }
            }


        } catch (e: Exception) {
            logger.error("Display error ${e.message}")

        }

        isRun = false
    }

    override fun toString(): String {
        return "DPSP Worker RUN :${isRun} JOBID:${pijob.id} "
    }

    override fun equals(other: Any?): Boolean {
        if (other is DSDPWorker) {
            var o = other as DSDPWorker
            if (pijob.id.equals(o.pijob.id))
                return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = ss.hashCode()
        result = 31 * result + dps.hashCode()
        result = 31 * result + pijob.hashCode()
        result = 31 * result + isRun.hashCode()
        result = 31 * result + (gpio?.hashCode() ?: 0)
        result = 31 * result + df.hashCode()
        result = 31 * result + d100.hashCode()
        return result
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DSDPWorker::class.java)
    }

}
