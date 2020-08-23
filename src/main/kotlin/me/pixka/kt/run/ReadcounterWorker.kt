package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ReadcounterWorker(val service: CountfgService, val p: Pijob, var pijs: PortstatusinjobService? = null,
                        var ips: IptableServicekt) :
        DefaultWorker(p, null, null, pijs, logger) {
    val om = ObjectMapper()
    override fun run() {
        startRun = Date()
        status = "Start run ${startRun}"
        logger.debug(status)
        Thread.currentThread().name = "JOBID:${pijob.id} D1Timer : ${pijob.name} ${startRun}"
        isRun = true

        try {
            var ip = getip(p.desdevice)
            if (ip != null) {
                var url3 = "http://${ip.ip}/readcount"
                status = "GET ${url3}"
                logger.debug(status)
                var ee = Executors.newSingleThreadExecutor()
                var get = HttpGetTask(url3)
                var f2 = ee.submit(get)
                try {
                    var re = f2.get(5, TimeUnit.SECONDS)
                    var countvalue = om.readValue<Countfg>(re, Countfg::class.java)
                    logger.debug("RETURN ${re}")
                    status = "RETURN ${re}"
                    if (countvalue.count!! > 0) {
                        countvalue.adddate = Date()
                       var s=  service.save(countvalue)
                        logger.debug("Saved count ${s}")
                    }
                    if (p.runtime != null) {
                        status = "Run ${p.runtime}"
                        logger.debug(status)
                        TimeUnit.SECONDS.sleep(p.runtime!!)
                    }
                    isRun = false
                } catch (e: Exception) {
                    logger.error(e.message)
                    status = "${e.message}"
                    isRun = false
                    throw e
                }

            }
        } catch (e: Exception) {
            logger.error(e.message)
            status = "${e.message}"
        }
        isRun = false

        status = "End job ${p.name}"
    }

    fun getip(tragetdevice: PiDevice?): Iptableskt? {
        try {
            var mac = tragetdevice?.mac
            return ips.findByMac(mac!!)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadcounterWorker::class.java)
    }

}