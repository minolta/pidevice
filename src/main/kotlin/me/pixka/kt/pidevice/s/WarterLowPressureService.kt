package me.pixka.kt.pidevice.s

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class WarterLowPressureService(val findJob: FindJob, val ntfs: NotifyService) {

    var reports = ArrayList<ReportLowPerssureObject>()
    var lowpressureCount = 0
    var maxcount = 10
    var canuse = true

    fun sendmessage(maxconfig: Pijob) {
        try {
            var notifyloop = maxconfig.tlow!!.toInt()
            for (i in 0..notifyloop) {
                if (maxconfig.token != null) {
                    ntfs.message("Low pressure", maxconfig.token!!)
                } else {
                    ntfs.message("Low pressure")
                }

                TimeUnit.SECONDS.sleep(1)
            }
        } catch (e: Exception) {
            logger.error("Error Send message ${e.message}")
        }
    }

    fun reportLowPerssure(deviceid: Long, psi: Double, pidevice: PiDevice? = null, job: Pijob? = null) {
        lowpressureCount++
        reports.add(ReportLowPerssureObject(deviceid, psi, pidevice, Date(), job))
        if (lowpressureCount >= maxcount) {

            //ปิดระบบน้ำเลย
            var maxconfig = getNotifyConfig()
            if (maxconfig != null) {
                sendmessage(maxconfig)
            }

            canuse = false
        }
    }

    fun setDefaultMaxCount() {
        var maxconfig = getNotifyConfig()
        if (maxconfig != null) {
            try {
                maxcount = maxconfig.thigh!!.toInt()
            } catch (e: Exception) {
                logger.error("Set Default ${e.message}")
                throw e
            }
        }
    }

    fun getNotifyConfig(): Pijob? {

        var config = findJob.findJobByName("MaxlowConfig")
        return config
    }

    fun reset(): Boolean {
        lowpressureCount = 0
        reports.clear()
        return true
    }

    var logger = LoggerFactory.getLogger(WarterLowPressureService::class.java)

}

class ReportLowPerssureObject(
    var pideviceid: Long,
    var psi: Double,
    var pidevice: PiDevice? = null,
    var lowtime: Date? = null, var job: Pijob? = null
)