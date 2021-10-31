package me.pixka.kt.run

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Cool down จะทำงานเมื่อ ความร้อนไปถึงจุดสูงสุดแล้ว จะ delay แล้วทำการ set port ตามกำหนดไว้
 */
class CooldownWorkerjob(job: Pijob, val mtp: MactoipService, val ntfs: NotifyService) : DWK(job), Runnable {
    override fun run() {

        isRun = true
        status = "Status run ${Date()}"
        if (tjob()) {
            go() //run
        }
        exitdate = Date()
        isRun = false
    }

    fun tjob(): Boolean {//สำหรับความร้อน
        try {
            var th = mtp.readTmp(pijob)
            var high = pijob.thigh?.toDouble()


            var value = th?.toDouble()
            var waittimehigh = pijob.hhigh!!.toInt()
            for (i in 1..waittimehigh) {
                if (value!! >= high!!) {
                    TimeUnit.SECONDS.sleep(5) //หยุดรออีกรอบ
                    th = mtp.readTmp(pijob)
                    value = th!!.toDouble()
                    if (value >= high) {
                        return waitlow()
                    }
                }

                status = "Wait high tmp ${i} tmp:${value}"
                TimeUnit.SECONDS.sleep(1)
            }
            status = "Wait high tmp timeout exit() wait high"
            return false //ความร้อนไม่

        } catch (e: Exception) {
            throw e
        }

    }

    /**
     * รอช่วงเย็น
     */
    fun waitlow(): Boolean {
        try {
            var low = pijob.tlow?.toDouble()
            var waittime = pijob.hlow!!.toInt()
            var tl = mtp.readTmp(pijob)
            var value = tl!!.toDouble()
            for (i in 0..waittime) {
                if (value <= low!!) {
                    TimeUnit.SECONDS.sleep(5)
                    tl = mtp.readTmp(pijob)
                    value = tl!!.toDouble()
                    if (value <= low) {
                        //now low is ok
                        return true
                    }
                }

                status = "Wait low tmp ${i} tmp:${value}"
                TimeUnit.SECONDS.sleep(1)
            }
            //low not in time
            status = "wait low tmp time out exit waitlow()"
            return false
        } catch (e: Exception) {
            throw e
        }
    }

    fun go() {

        var ports = mtp.getPortstatus(pijob)

        if (ports != null) {
            ports.filter { it.enable == true }.forEach {
                try {
                    mtp.setport(it)
                } catch (e: Exception) {
                    if (pijob.token != null) {
                        ntfs.message("Set port ERROR ${pijob.name}  port ${it.portname} ", pijob.token!!)
                    }
                }
            }
        }

    }
}