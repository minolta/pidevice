package me.pixka.kt.run

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PortstatusinjobService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ReadcounterWorker(val service: CountfgService, val p: Pijob, var pijs: PortstatusinjobService? = null,
                        var ips: String,val lgs:LogService,val httpService: HttpService) :
        DefaultWorker(p, null, null, pijs, logger) {
    val om = ObjectMapper()

    var exitdate:Date?=null
    fun setEnddate() {
        var t = 0L
        if (pijob.waittime != null)
            t = pijob.waittime!!
        if (pijob.runtime != null)
            t += pijob.runtime!!

        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, t.toInt())
        exitdate = calendar.time
        if (t == 0L)
            isRun = false//ออกเลย
    }

    override fun run() {

        startRun = Date()
        status = "Start run ${startRun}"
        logger.debug(status)
        Thread.currentThread().name = "JOBID:${pijob.id} D1Timer : ${pijob.name} ${startRun}"
        isRun = true

        try {

            var url3 = "http://${ips}/readcount"
            status = "GET ${url3}"
            logger.debug(status)

            try {
                val re = httpService.get(url3,5000)
                var countvalue = om.readValue<Countfg>(re, Countfg::class.java)
                logger.debug("RETURN ${re}")
                status = "RETURN ${re}"
                if (countvalue.count!! > 0) {
                    countvalue.adddate = Date()
                    var s = service.save(countvalue)
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
                lgs.createERROR("${e.message}",Date(),"ReadcounterWorker",
                "","32","run()",pijob.desdevice?.mac,pijob.refid)
                throw e
            }


        } catch (e: Exception) {
            logger.error(e.message)
            lgs.createERROR("${e.message}",Date(),"ReadcounterWorker",
                    "","26","run()",pijob.desdevice?.mac,pijob.refid)
            status = "${e.message}"
        }
        isRun = false
        status = "End job ${p.name}"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadcounterWorker::class.java)
    }

}