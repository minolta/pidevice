package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.sql.Time
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Displaydhtvalue(val dps: DisplayService, val dhts: DhtvalueService, val dbcfg: DbconfigService) {
    var df = DecimalFormat("##.#")
    @Scheduled(fixedDelay = 20000)
    fun run() {

        var run = dbcfg.findorcreate("Displaydhtvalue", "true").value
        logger.debug(" Can run:${run}")
        if (run?.indexOf("true") == -1) {
            //not rune display dhtvalue
            logger.debug("exit ")
            return
        }
        var count = 0
        while (dps.lock) {
            //รอจนกว่าจะแสดงได้
            println("whait for lock DHT")
            TimeUnit.MILLISECONDS.sleep(200)
            count++
            if(count>5) {
                logger.error("Display is busy")
                return
            }
        }


        var dot = dps.lockdisplay(this)
        var last = dhts.last()
        if (last != null) {
            dot.showMessage("DHT value ")
            TimeUnit.SECONDS.sleep(1)
            dot.print("H")
            TimeUnit.SECONDS.sleep(1)
            dot.clear()
            dot.print(df.format(last.h))
            TimeUnit.SECONDS.sleep(3)
            dot.clear()
            dot.print("T")
            TimeUnit.SECONDS.sleep(1)
            dot.clear()
            dot.print(df.format(last.t))
            TimeUnit.SECONDS.sleep(3)
        }
        dps.unlock(this)
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Displaydhtvalue::class.java)
    }
}