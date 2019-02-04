package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pidevice.s.InfoService
import me.pixka.pibase.s.PideviceService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.math.BigDecimal
import java.util.*


@Component
@Profile("pi")
class Readlocalpressure(val infoService: InfoService, val ps: Piio,
                        val prs: PressurevalueService, val pideviceService: PideviceService) {
    var mapper = ObjectMapper()
    @Scheduled(fixedDelay = 5000)
    fun run() {

        try {

            var filepath = System.getProperty("pressurefile")
            var file = File(filepath)
            val obj = mapper.readValue(file, Analogvalue::class.java)
            infoService.A0 = obj
            logger.debug("A0 ${obj}")
            var pv = PressureValue()
            var p = ps.wifiMacAddress()
            logger.debug("Find MAC :[${p}]")
            var pd = pideviceService.findByMac(p)


            if (pd == null) {
                var vvv = PiDevice()
                vvv.mac = p
                pd = pideviceService.save(vvv)
             }
            pv.device = pd
            pv.pressurevalue = obj.psi
            var v = prs.save(pv)
            logger.debug("Save ${v}")

        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Readlocalpressure::class.java)
    }
}


class Analogvalue(var name: String? = null, var percent: BigDecimal? = null,
                  var voltage: BigDecimal? = null,
                  var adddate: Date? = null, var psi: BigDecimal? = null) {
    override fun toString(): String {
        return "${name}  ${percent}% V[${voltage}] DATE[${adddate}] PSI[${psi}]"
    }
}