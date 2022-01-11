package me.pixka.kt.run

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.worker.PressureWorker
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * สำหรับวัดการใช้แรงดันนานเท่าที่กำหนดแล้วทำงาน
 */
class PressureOverWorker(p: Pijob, mactoipService: MactoipService, n: NotifyService) :
    PressureWorker(p, mactoipService, n) {

    override fun run() {

        isRun = true
        startRun = Date()

        try {
            if (checkinrnage()) {
                if (pijob.token != null) {
                    ntfs.message("Over pressure $pijob.name", pijob.token!!)
                    status = "Over pressure $pijob.name"
                } else {
                    ntfs.message("Over pressure $pijob.name")
                    status = "Over pressure $pijob.name"
                }
                setPort()
                status = "exit normal"

            } else {
                status = "Not in range"
                isRun = false
            }
        } catch (e: Exception) {
            status = "Got error ${e.message}"
            isRun = false
        }

        exitdate = findExitdate(pijob)

    }


    /**
     * สำหรับรอว่าอยู่ใน ช่วงเวลาทีกำหนด
     */
    fun checkinrnage(): Boolean {
        //tlow และ thigh เป็นช่วงแรงดัน
        //hlow  เป็นเวลาที่กำหนด

        var pd = pijob.desdevice

        if (pd != null) {
            var pl = pijob.tlow!!.toDouble()
            var ph = pijob.thigh!!.toDouble()
            var time = pijob.hlow!!.toInt()

            var t = mactoipService.readPressure(pd, 2000)
            for (l in 0..time) {
                if (t!! in pl..ph) {
                    status = "Pressure in rang ${pl} < ${t} < ${ph}"
                } else {
                    status = "Pressure not in rang ${pl} < ${t}  < ${ph}"
                    return false
                }

                TimeUnit.SECONDS.sleep(1)
            }

            //ถ้าครบแล้วก็
            return true
        }

        return false
    }

}