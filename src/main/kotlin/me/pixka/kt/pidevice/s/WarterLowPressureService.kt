package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import org.springframework.stereotype.Service
import java.util.*

@Service
class WarterLowPressureService {

    var reports = ArrayList<ReportLowPerssureObject>()
    var lowpressureCount = 0
    var maxcount = 10
    var canuse = true


    fun reportLowPerssure(deviceid: Long, psi: Double, pidevice: PiDevice? = null,job:Pijob?=null) {
        lowpressureCount++
        reports.add(ReportLowPerssureObject(deviceid, psi, pidevice, Date(),job))
        if (lowpressureCount >= maxcount) {
            //ปิดระบบน้ำเลย
            canuse = false
        }
    }

    fun reset(): Boolean {
        lowpressureCount = 0
        reports.clear()
        return true
    }
}

class ReportLowPerssureObject(
    var pideviceid: Long,
    var psi: Double,
    var pidevice: PiDevice? = null,
    var lowtime: Date? = null,var job:Pijob?=null
)