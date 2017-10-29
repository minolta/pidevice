package me.pixka.kt.pidevice.t

import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.pibase.d.DS18value
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.Ds18valueService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class Read18b20s(val ds: Ds18valueService, val io: Piio, val dss: DS18sensorService, val err: ErrorlogService) {

    var oldvalue = ArrayList<dsbuf>()
    @Scheduled(fixedDelay = 10000)
    fun read() {
        try {
            var values = io.reads()
            if (values != null) {
                for (value in values) {
                    var old = find(value)
                    if (old == null || old.value.compareTo(value.t) != 0) {
                        logger.info("New DS18value ${value} old value:${old?.value}")
                        ds.save(value)
                        push(value)

                    }
                }
            }

        } catch (e: Exception) {
            logger.error("Read DS18 ${e.message}")
            err.n("Read Read18b20", "17-21", "${e.message}")

        }
    }

    /**
     * find change value
     */
    fun find(ds: DS18value): dsbuf? {
        for (d in oldvalue) {
            if (d.id == ds.id.toInt())
                return d
        }
        return null
    }

    fun push(ds: DS18value): Boolean? {

        for (d in oldvalue) {
            if (d.id == ds.id.toInt()) {
                d.value = ds.t!!
                return true
            }
        }
        var buf = dsbuf(ds.id.toInt(), ds.t!!)
        oldvalue.add(buf)

        return false


    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Read18b20s::class.java)
    }
}

class dsbuf(var id: Int, var value: BigDecimal) {

}