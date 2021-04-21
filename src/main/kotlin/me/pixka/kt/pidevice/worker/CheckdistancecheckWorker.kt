package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class CheckdistancecheckWorker(p: Pijob, var ntfs: NotifyService, var ms: MactoipService) : DWK(p), Runnable {
    override fun run() {
        startRun = Date();
        isRun = true

        try {
            var d = ms.readDistance(pijob.desdevice!!.ip!!)
            status = "rang is ${d}"
            if (checkRang(d!!))
            {
                var token = pijob.token
                if(token==null)
                    ntfs.message("In range ${pijob.tlow} <= ${d} <= ${pijob.thigh}")
                else
                    ntfs.message("In range ${pijob.tlow} <= ${d} <= ${pijob.thigh}",token)

            }
         exitdate = findExitdate(pijob)

        } catch (e: Exception) {
            logger.error("ERROR ${pijob.name} Message ${e.message}")
            isRun=false

        }
    }

    /**
     * ใช้สำหรับ ตรวจสอบว่าอยู่ในช่วงที่กำหนดหรือเปล่า
     */
    fun checkRang(d: Long): Boolean {
        var l = pijob.tlow!!.toLong()
        var h = pijob.thigh!!.toLong()
        status = "Rang ${l} <= ${d} <= ${h}"
        if (l <= d && d <= h)
            return true


        return false


    }

    var logger = LoggerFactory.getLogger(CheckdistancecheckWorker::class.java)
}