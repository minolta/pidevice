package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.DHTObject
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DhtvalueService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*

class ReadDhtWorker(pijob: Pijob, val pds: PideviceService, var mtp: MactoipService,var dhts:DhtvalueService)
    : DWK(pijob), Runnable {
    override fun run() {
        var t = 0.0
        var h = 0.0
        try {
            isRun = true
            startRun = Date()
            logger.debug("Start run ${startRun}")
            Thread.currentThread().name = "JOBID:${pijob.id} ReadDHT ${pijob.name} ${startRun}"
            var ip = pijob.desdevice?.ip
            var re = mtp.http.get("http://${ip}", 10000)
            var dht = mtp.om.readValue<DHTObject>(re)
            var dhtvalue = Dhtvalue()
            dhtvalue.valuedate = Date()
            dhtvalue.pidevice = pijob.desdevice
            dhtvalue.t = dht.t
            dhtvalue.h = dht.h
            if (dht.t != null)
                t = dht.t?.toDouble()!!
            if (dht.h != null)
                h = dht.h?.toDouble()!!
            var r = dhts.save(dhtvalue)
            logger.debug("Save ${r}")
            status = "Save ${r}"

        } catch (e: Exception) {
            logger.error("Read DHT task Error ${e.message}")
            mtp.lgs.createERROR("Read DHT task Error ${e.message}",
                    Date(), "ReadDhtWorker", Thread.currentThread().name, "run",
                    "", pijob.desdevice?.mac, pijob.refid)
            status = "ERROR ${e.message}"
            isRun = false
        }

        exitdate = findExitdate(pijob)
        if (exitdate == null)
            isRun = false
        status = "End Read DHT task DHT T:${t} H:${h}"
        logger.debug("End Read DHT task")
    }


    var logger = LoggerFactory.getLogger(ReadDhtWorker::class.java)


}