package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.s.DS18sensorService
import me.pixka.kt.pibase.s.Ds18valueService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@RestController
@Profile("pi")
class DS18Control(val service: Ds18valueService, val dss: DS18sensorService, val io: Piio) {
    @CrossOrigin
    @RequestMapping(value = ["/ds18value"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): DS18value? {

        try {
            val ds = dss.findForread()
            var last: DS18value? = null
            if (ds != null) {

                last = service.lastBysensor(ds.id)
                logger.debug("[ds18value] read by forread sensor : " + last)
            } else {
                last = service.last()
                logger.debug("[ds18value] read by default sensor : " + last)
            }
            logger.debug("[ds18value ] read value : " + last)
            logger.debug("[ds18value] end status")
            return last
        } catch (e: Exception) {
            logger.error("[ds18value] Error: " + e.message)
        }

        return null
    }

    @CrossOrigin
    @RequestMapping(value = ["/ds18valuebysensor/{sensor}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(@PathVariable("sensor") sensor: String): DS18value? {

        //เปลียนไป read direct
        var values = io.reads()
        if (values != null) {
            for (value in values) {

                if (value.ds18sensor?.name.equals(sensor)) {
                    return value
                }
            }
        }

        return null
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(DS18Control::class.java)
    }
}