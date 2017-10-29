package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.pibase.d.Dhtvalue
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class ReadDht(val service: DhtvalueService, val io: Piio, val err: ErrorlogService) {
    val hlimit = BigDecimal("100")
    var old: Dhtvalue? = null
    @Scheduled(fixedDelay = 5000)
    fun read() {
        try {
            var o: Dhtvalue? = io.readDHT()

            if (o != null) {// มีข้อมูล
                logger.info("DHT value :${o}")
                if (o.h?.compareTo(hlimit)!! > 0)//Fix bug read h value is over 100 %
                {
                    logger.error("Dthvalue is over 100 ${o?.h}")
                    o.h = BigDecimal(100)

                }
                if (old == null || o.t?.compareTo(old?.t) != 0 || o.h?.compareTo(old?.h) != 0)//ถ้าข้อมูลเปลียนให้บันทึก
                {
                    o = service!!.save(o)
                    logger.info("Save Change ${old} -> ${o}   ${Date()}")
                    old = o
                }

            }
            logger.debug("[savedht] ######## DHT value T: ${o?.t} H:${o?.h}")
        } catch (e: Exception) {
            logger.error("[savedht] Save DHT Error " + e.message)
            err.n("Read dht", "19-25", "${e.message}")

        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDht::class.java)
    }
}