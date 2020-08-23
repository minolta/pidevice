package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@Profile("pi", "lite")
class ReadDht(val service: DhtvalueService, val io: Piio) {
    val hlimit = BigDecimal("100")
    var old: Dhtvalue? = null
    @Scheduled(initialDelay = 3000, fixedDelay = 60000)
    fun read() {

        var canread = System.getProperty("readdht", "true")
        if (!canread.equals("true")) {
            logger.info("Not run read dht")
            return
        }


        try {
            var o: Dhtvalue? = io.readDHT()
            if (o != null) {// มีข้อมูล
                logger.info("DHT value :${o}")
                if (o.h?.compareTo(hlimit)!! > 0)//Fix bug read h value is over 100 %
                {
                    logger.error("Dthvalue is over 100 ${o.h}")
                    o.h = BigDecimal(100)

                }

                if (old == null || o.t?.compareTo(old?.t) != 0 || o.h?.compareTo(old?.h) != 0)//ถ้าข้อมูลเปลียนให้บันทึก
                {


                    if (old == null) {
                        o = service.save(o)
                        logger.debug("Save New ${o}")
                        old = o
                    } else {
                        if (checkvaluetosave(o.t!!, old!!.t!!) || checkvaluetosave(o.h!!, old!!.h!!)) {

                            o = service.save(o)
                            logger.debug("Save Value change over rang  ${o}")
                            old = o
                        }
                    }
                }

            }
            logger.debug("[savedht] ######## DHT value T: ${o?.t} H:${o?.h}")
        } catch (e: Exception) {
            logger.error("[savedht] Save DHT Error " + e.message)

        }


    }


    fun checkvaluetosave(src: BigDecimal, des: BigDecimal): Boolean {

        var rangertosave = BigDecimal(System.getProperty("dhtvaluerangtosave", "0.5"))
        var result = src.subtract(des)

        var over05 = result.abs()
        logger.debug("Diff ${over05}")
        if (over05.compareTo(rangertosave) > 0) {
            logger.debug("Value diff Have to save")
            return true

        }

        logger.debug("Not save value")
        return false // not over rang

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDht::class.java)
    }
}