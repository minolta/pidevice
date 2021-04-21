package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * สำหรับ notify worker
 */
class NotifytmpWorker(
    p: Pijob, var mac: MactoipService,
    var target: String, var notify: NotifyService
) : DWK(p), Runnable {
    var token = System.getProperty("pressurenotify")
    override fun run() {
        try {
            startRun = Date()
            isRun = true
            status = "Status run"
            var tmp = mac.readTmp(pijob)!!.toDouble()
            println(tmp)
            if (tmp != null) {
                var low = pijob.tlow?.toDouble()
                var high = pijob.thigh?.toDouble()

                if (low!! <= tmp && tmp <= high!!) {
                    status = "In Rang ${low}  <= ${tmp} => ${high}"
                    if (token == null)
                        token = pijob.token
                    //in rang
                    var d: String? = ""
                    if (pijob.description != null)
                        d = pijob.description
                    notify.message("Tmp In Rang ${low}  <= ${tmp} <= ${high} JOB:${pijob.name} description:${d}", token)

                    if (pijob.runtime != null) {
                        status = "Run time ${pijob.runtime}"
                        TimeUnit.SECONDS.sleep(pijob.runtime!!)
                    }

                    if (pijob.waittime != null) {
                        status = "Wait time ${pijob.runtime}"
                        TimeUnit.SECONDS.sleep(pijob.waittime!!)
                    }
                } else {
                    status = "Not In Rang ${low}  <= ${tmp} => ${high}"
                    logger.debug("Not in rang")
                }


                exitdate = findExitdate(pijob)
                status = "End"


            }
        } catch (e: Exception) {
            logger.error(e.message)
            status = e.message!!
            isRun=false
        }

        status = "End job"
    }

    var logger = LoggerFactory.getLogger(NotifytmpWorker::class.java)
}