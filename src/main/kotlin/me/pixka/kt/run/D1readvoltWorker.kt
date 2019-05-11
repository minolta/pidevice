package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.PressureValue
import me.pixka.kt.pibase.d.PressurevalueService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class D1readvoltWorker(p: Pijob, val readvalue: ReadUtil, val pijs: PortstatusinjobService,val pss:PressurevalueService) :
        DefaultWorker(p, null, readvalue, pijs, logger) {
    override fun run() {
        isRun = true
        startRun = Date()
        try {
            var a0value = readvalue.readA0(pijob.desdevice_id!!)
            //var c = pijob.hlow?.toFloat()

            var psv = PressureValue()
            psv.device_id = pijob.desdevice_id
            psv.valuedate = Date()
            psv.rawvalue = BigDecimal(a0value.toString())
            pss.save(psv)

            var run  = pijob.runtime
            TimeUnit.SECONDS.sleep(run?.toLong()!!)
            TimeUnit.SECONDS.sleep(pijob.waittime?.toLong()!!)
        }
        catch (e:Exception)
        {
            isRun=false
            logger.error(e.message)
            throw e
        }

        isRun=false

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(D1readvoltWorker::class.java)
    }
}