package me.pixka.kt.run

import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.pibase.d.Pijob
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Profile("pi")
class DSDPWorker(
        val ss: SensorService,
        val dps: DisplayService,
        var pijob: Pijob) : Runnable, PijobrunInterface {


    override fun getPijobid(): Long {
        if (pijob != null) {
            var id = pijob?.id
            return id!!
        }

        return 0
    }

    var isRun: Boolean = false

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
    override fun run() {
        logger.info("Run DSDPWork")
        if (pijob?.desdevice_id != null && pijob.ds18sensor_id != null) {

            logger.debug(" JOB ${pijob}")
            var dsvalue = ss.readDsOther(pijob?.desdevice_id!!, pijob?.ds18sensor_id!!) //อ่านค่าตัวอื่น
            if (dsvalue != null) {
                while (dps.lock) {
                    logger.debug("Wait for lock display")
                    TimeUnit.MILLISECONDS.sleep(200)
                }
                dps.lockdisplay(this)
                dps.dot.print(df.format(dsvalue.t))
                dps.unlock(this)
                isRun = false

            } else
                logger.error("Read other fail ${pijob}")
        }


        isRun = false
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(DSDPWorker::class.java)
    }

}
