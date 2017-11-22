package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.Ds18valueService
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
                      val dbcfg: DbconfigService) {

    var df = DecimalFormat("##.0")
    val df100 = DecimalFormat("###")

    @Scheduled(initialDelay = 5000,fixedDelay = 30000)
    fun run() {
        logger.info("Run Display DS 18b20 value")
        var run = dbcfg.findorcreate("displaydsvalue", "true").value
        logger.debug(" Can run:${run}")
        if (run?.indexOf("true") == -1) {
            //not rune display dhtvalue
            logger.error("exit DS display job ")
            return
        }
        var sensor = dss.all() //sensor ทั้งหมด
        logger.debug("All sensor : ${sensor}")

        if (sensor != null) {

            var buf = ArrayList<Dssensorforfindlast>()
            for (s in sensor) {
                var t = dsvs.lastBysensor(s.id)

                if (t != null) {
                    logger.debug("sensor: ${s} T:${t.t}")
                    var dsfl = Dssensorforfindlast(s, t)
                    buf.add(dsfl)
                    logger.debug("Add to display-> ${dsfl}")
                }
            }


            logger.debug("All for display ${buf} size:${buf.size}")

            try {
                if (buf.size > 0) {
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
                        if(dd.length>4)
                            dd = "*"+df100.format(b.ds18value?.t)

                        dot.print(dd)
                        TimeUnit.SECONDS.sleep(5)
                        dot.clear()
                    }
                    dps.unlock(this)

                }
            }catch (e:Exception)
            {
                logger.error("Error ${e.message}")
            }
            finally {
                //dps.unlock(this)
                logger.info("Finally")
            }
        }


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Displaydsvalues::class.java)
    }
}