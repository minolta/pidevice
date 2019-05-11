package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.pibase.s.DhtvalueService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class ReadDhtWorker(pijob: Pijob, val dhts: Dhtutil, val dhtvalueService: DhtvalueService)
    : DefaultWorker(pijob, null, null, null, logger) {
    override fun run() {
        try {
            isRun = true
            startRun = Date()
            logger.debug("Start run ${startRun}")
            var dht = dhts.readByPijob(pijob)
            if (dht != null) {
                dht.valuedate = Date()
                dht.pidevice = pijob.desdevice
                var r = dhtvalueService.save(dht)
                logger.debug("Save ${r}")
                status = "Save ${r}"
            }

            if (pijob.waittime != null) {
                logger.debug("Wait next run ${pijob.waittime}")
                status = "Wait next run ${pijob.waittime}"
                TimeUnit.SECONDS.sleep(pijob.waittime!!.toLong())
            }
        } catch (e: Exception) {
            logger.error("Read DHT task Error ${e.message}")
            status = "ERROR ${e.message}"
        }

        isRun = false
        status = "End Read DHT task"
        logger.debug("End Read DHT task")
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(ReadDhtWorker::class.java)
    }

}