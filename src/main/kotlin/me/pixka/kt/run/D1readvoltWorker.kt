package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Vbatt
import me.pixka.kt.pibase.d.VbattService
import me.pixka.kt.pibase.o.VbattObject
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

class D1readvoltWorker(
    job: Pijob,
    val pss: VbattService,
    var mtp: MactoipService
) : DWK(job), Runnable {
    override fun run() {
        isRun = true
        startRun = Date()
        Thread.currentThread().name = "JOBID:${pijob.id} D1readvolt : ${pijob.name} ${startRun}"
        try {
            if (pijob.desdevice?.mac != null) {
                var ip = mtp.mactoip(pijob.desdevice?.mac!!)
                var re = mtp.http.get("http://${ip}", 20000)
                var v = mtp.om.readValue<VbattObject>(re)
                var psv = Vbatt()
                psv.pidevice_id = pijob.desdevice_id
                psv.pidevice = pijob.desdevice
                psv.valuedate = Date()
                psv.v = v.batt_volt
                pss.save(psv)
                status = "Wait ${pijob.waittime?.toLong()}"
                exitdate = findExitdate(pijob)
            } else {
                logger.error("${pijob.name} : Mac not have")
                status = "Noht have mac "
                isRun = false
                mtp.lgs.createERROR(
                    "Not have mac", Date(),
                    "D1readvoltWorker", Thread.currentThread().name, "24", "run()",
                    ""
                )
            }

        } catch (e: Exception) {
            isRun = false
            logger.error("${pijob.name} ${e.message}")
            status = e.message.toString()
            mtp.lgs.createERROR(
                e.message!!, Date(), "D1readvoltWorker", Thread.currentThread().name,
                "23", "Run"
            )
            throw e
        }
        status = "End job and wait"
    }


    var logger = LoggerFactory.getLogger(D1readvoltWorker::class.java)


}

@JsonIgnoreProperties(ignoreUnknown = true)
class Espstatus(var batt_volt: BigDecimal? = null, var errormessage: String? = null)