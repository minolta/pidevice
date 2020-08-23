package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.s.DS18sensorService
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.Ds18valueService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Displaydsvalues(val dps: DisplayService,
                      val dss: DS18sensorService,
                      val dsvs: Ds18valueService,
                      val io: Piio) {

    var df = DecimalFormat("##.0")
    val df100 = DecimalFormat("###")

    @Scheduled(initialDelay = 5000, fixedDelay = 30000)
    fun run() {
        logger.info("Run Display DS 18b20 value")
        var run = System.getProperty("displaydsvalue", "false")
        logger.debug(" Can run:${run}")
        if (run?.indexOf("true") == -1) {
            //not rune display dhtvalue
            logger.error("exit DS display job ")
            return
        }

        var buf = findvalues()

        if (buf.size > 0) {
            display(buf)
        }
      


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Displaydsvalues::class.java)
    }


    fun display(buf: List<Dssensorforfindlast>) {
        logger.debug("start booking dpslay:")
        var count = 0
        while (dps.lock) {
            //wait lock display
            println("whait for lock DSVALUE")
            TimeUnit.MILLISECONDS.sleep(200)
            count++
            if (count > 20) {
                logger.error("Display Busy")
                return
            }
        }
        var dot = dps.lockdisplay(this)
        logger.debug("lock for ds value display ")
        for (b in buf) {
            dot.showMessage("sensor:${b.dssensor?.name}")
            TimeUnit.SECONDS.sleep(1)
            dot.clear()
            var dd = df.format(b.ds18value?.t)
            if (dd.length > 4)
                dd = "*" + df100.format(b.ds18value?.t)

            dot.print(dd)
            TimeUnit.SECONDS.sleep(5)
            dot.clear()
        }
        dps.unlock(this)
    }


    fun findvalues(): ArrayList<Dssensorforfindlast> {
        var buf = ArrayList<Dssensorforfindlast>()
        var values = io.reads()
        if (values != null)
            for (dv in values) {
                var dsfl = Dssensorforfindlast(dv.ds18sensor, dv)
                buf.add(dsfl)
                logger.debug("Add to display-> ${dsfl}")
            }

        return buf
    }
}