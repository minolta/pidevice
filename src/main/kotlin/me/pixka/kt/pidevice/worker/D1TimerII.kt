package me.pixka.kt.pidevice.worker

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class D1TimerII(job: Pijob, var mtp: MactoipService, val line: NotifyService) : DWK(job), Runnable {
    var maxrun = 0
    var maxwait = 0

    //    var df = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
    override fun run() {
        isRun = true
        startRun = Date()
        var t = mtp.readTmp(pijob)
        try {
            //ถ้า ตอนนี้ มันสูงกว่า ให้ออกเลยไม่ทำงานแล้ว
            if (t == null || t.toDouble() > pijob.tlow!!.toDouble()) {
                status = "Low tmp is high then tlow exit job"
                isRun = false
                throw Exception("Low tmp is high then tlow exit job")
            }
        }catch (e:Exception)
        {
            logger.error(e.message)
            isRun = false
            throw e

        }

        try {
            if (waitlowtmp()) {
                //ถ้าถึงแล้ว
                if (waithightmp()) {
                    setPort()
                    exitdate = findExitdate(pijob, (maxrun + maxwait).toLong())
                } else {
                    status = "ความร้อนไม่ถึงขันสูง"
                    isRun = false//ไม่ถึงความร้อนสูง
                }
            } else {
                status = "ความร้อนไม่ถึงที่กำหนด"
                isRun = false //ไม่ถึงขันตำ
            }
        } catch (e: Exception) {
            isRun = false
            logger.error("ERROR ${e.message}")
            //ERROR
        }

    }

    fun setPort() {

        try {
            var ports = mtp.getPortstatus(pijob)

            if (ports != null) {
                ports.forEach {
                    try {
                        var re = mtp.setport(it)
                        var token: String? = null
                        if (token == null)
                            token = pijob.token
                        if (token != null)
                            for (i in 0..10) {
                                line.message("เริ่มจับเวลา ${pijob.name} ${df.format(Date())} ", token)
                                TimeUnit.SECONDS.sleep(1)
                            }
                        if (it.runtime != null && it.runtime!!.toInt() > maxrun) {
                            maxrun = it.runtime!!.toInt()
                        }
                        if (it.waittime != null && it.waittime!!.toInt() > maxwait) {
                            it.waittime!!.toInt()
                        }

                    } catch (e: Exception) {
                        logger.error(e.message)
                    }
                }
            }
        }catch (e:Exception)
        {
            logger.error("Set port ERROR ${e.message}")
        }
    }

    //รจนกว่าจะถึงข้างบนถ้าถึงให้ทำการตั้งเวลาเลย
    fun waithightmp(): Boolean {
        try {
            var waittime = 10
            if (pijob.hlow != null) {
                waittime = pijob.hlow!!.toInt()
            }
            for (i in 0..waittime) {

                var t = mtp.readTmp(pijob)
                if (t != null && t.toDouble() >= pijob.thigh!!.toDouble()) {
                    return true
                }

                TimeUnit.SECONDS.sleep(1)
            }
            return false
        } catch (e: Exception) {
            logger.error("waithightmp ERROR ${e.message}")
            throw e
        }

    }

    //รอขันตำก่อนถ้าไม่ถึง 10 วิ หรือ ตาม hlow ถ้าถึงออกเลยแต่ถ้าไม่ถึงออก
    fun waitlowtmp(): Boolean {
        try {

            var waittime = 10
            if (pijob.hlow != null) {
                waittime = pijob.hlow!!.toInt()
            }
            for (i in 0..waittime) {
                var t = mtp.readTmp(pijob)
                if (t != null && (t.toDouble() >= pijob.tlow!!.toDouble())) {
                    return true
                }

                TimeUnit.SECONDS.sleep(1)
            }
            return false
        } catch (e: Exception) {
            logger.error("waitlowtmp ERROR ${e.message}")
            throw e
        }

    }

    internal var logger = LoggerFactory.getLogger(D1TimerII::class.java)
}