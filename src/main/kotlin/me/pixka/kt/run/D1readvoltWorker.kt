package me.pixka.kt.run

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.pidevice.d.Vbatt
import me.pixka.kt.pidevice.d.VbattService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class D1readvoltWorker(p: Pijob, val readvalue: ReadUtil, val pijs: PortstatusinjobService, val pss: VbattService) :
        DefaultWorker(p, null, readvalue, pijs, logger) {
    val om = ObjectMapper()
    override fun run() {
        isRun = true
        startRun = Date()
        Thread.currentThread().name = "JOBID:${pijob.id} D1readvolt : ${pijob.name} ${startRun}"
        try {
            var p = pijob
//            var ip = getip(pijob.desdevice)
            var ip = readvalue.findIp(pijob.desdevice!!)
            if (ip != null) {

                var url3 = "http://${ip.ip}"
                var ee = Executors.newSingleThreadExecutor()
                var get = HttpGetTask(url3)
                var f2 = ee.submit(get)
                try {
                    var re = f2.get(5, TimeUnit.SECONDS)
                    var espstatus = om.readValue<Espstatus>(re, Espstatus::class.java)

                    //TODO ต้องอ่าน status จากปั๊มตรงนี้ แล้วเอาค่า batt_volt มา save เข้าระบบ
                    var psv = Vbatt()
                    psv.pidevice_id = pijob.desdevice_id
                    psv.pidevice = pijob.desdevice
                    psv.valuedate = Date()
                    psv.v = espstatus.batt_volt
                    this.status = "Vbatt :" + psv.v
                    pss.save(psv)
                } catch (e: Exception) {
                    logger.debug("Readvbatt status error ${e.message}")
                }
            }
            var run = pijob.runtime
            TimeUnit.SECONDS.sleep(run?.toLong()!!)
            status = "Wait ${pijob.waittime?.toLong()}"
            TimeUnit.SECONDS.sleep(pijob.waittime?.toLong()!!)
        } catch (e: Exception) {
            isRun = false
            logger.error(e.message)
            status = e.message
            throw e
        }

        isRun = false
        status = "End job"

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(D1readvoltWorker::class.java)
    }


}

@JsonIgnoreProperties(ignoreUnknown = true)
class Espstatus(var batt_volt: BigDecimal? = null)