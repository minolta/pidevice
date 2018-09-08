package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.pibase.s.Ds18valueService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class Readlocaltemp(val io: Piio, val ds: Ds18valueService ,val pis:PideviceService) {


    @Scheduled(fixedDelay = 10000)
    fun readlocal() {
        logger.debug("Save Local Temp")
        var valuefromlocalsensor = io.reads()

        logger.debug("Found ${valuefromlocalsensor} ${valuefromlocalsensor?.size}")
        if (valuefromlocalsensor != null && valuefromlocalsensor.size > 0) {
            logger.debug(" Goto Save local ds")
            for (v in valuefromlocalsensor) {
                try {
                    logger.debug("Save ${v}")
                    var me = io.getPidevice()
                    var pd = pis.findByMac(me.mac!!)
                    if(pd==null)
                        pd = pis.create(me.mac!!,me.mac!!)
                    v.pidevice = pd
                    var save = ds.save(v)
                    logger.debug("Save:=====> ${save}")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
            }
        }
        logger.debug("End Save local temp")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Readlocaltemp::class.java)
    }
}