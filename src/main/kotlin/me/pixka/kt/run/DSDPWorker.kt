package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Profile("pi")
class DSDPWorker(
        val ss: SensorService,
        val dps: DisplayService,
        var pijob: Pijob) : Runnable, PijobrunInterface {


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

    companion object {
        internal var logger = LoggerFactory.getLogger(DSDPWorker::class.java)
    }

}
