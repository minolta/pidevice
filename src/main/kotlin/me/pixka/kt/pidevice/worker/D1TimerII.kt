package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.c.Statusobj
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
        status = "Start run"

        try {
            var t = mtp.readTmp(pijob, 10000)
            status = "Current tmp ${t}"
            //ถ้า ตอนนี้ มันสูงกว่า ให้ออกเลยไม่ทำงานแล้ว
            if (t == null || t.toDouble() > pijob.thigh!!.toDouble()) {
                status = "Low tmp is high then thigh exit job"
                isRun = false
                return //ออกเลย
//                throw Exception("Low tmp is high then thigh exit job")
            }
        } catch (e: Exception) {
            status = "Have error ${e.message}"
            logger.error(e.message)
            isRun = false
            throw e

        }

        try {
            if (waitlowtmp(pijob)) {
                //ถ้าถึงแล้ว
                status = "ความเลยขั้นต่ำไปแล้ว"
                if (waithightmp(pijob)) {
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
            status = "Check Tmp ERROR ${e.message}"
            logger.error("ERROR ${e.message}")
            //ERROR
        }
//        status = "Exit normal"
//        isRun = false
    }

    fun set(mac: String, delay: Int, value: Int, port: String): Boolean {
        try {
            var ip = mtp.mactoip(mac)
            if (ip != null) {
                status = "TRy to set port http://${ip}/run?delay=${delay}&port=${port}&value=${value}"
                var re = mtp.http.getNoCache("http://${ip}/run?delay=${delay}&port=${port}&value=${value}", 30000)

                try {
                    var statusobj = mtp.om.readValue<Statusobj>(re)
                    status = "Set status ${statusobj.status}"
                    if (statusobj.status != null && statusobj.status.equals("ok")) {
                        status = "Set port ok"
                        return true
                    }
                    status = "Status not ok"
                    return false
                } catch (e: Exception) {
                    status = "set ERROR ${e.message}"
                    logger.error("Get setport status ERROR ${e.message}")
                    throw e
                }
            } else {
                status = "Ip not found Mac ${mac} "
                throw Exception("IP NOT Found ")
            }
        } catch (e: Exception) {
            status = "Set() ERROR ${e.message}"
            logger.error("Set() ERROR ${e.message}")
            throw e
        }
    }

    fun setPort() {

        try {
            var ports = mtp.getPortstatus(pijob)

            if (ports != null) {
                ports.forEach {
                    try {

                        var canset = set(it.device?.mac!!, it.runtime!!.toInt(), 1, it.portname?.name!!)

                        var token: String? = null
                        if (token == null)
                            token = pijob.token

                        if (token != null && canset) {
                            for (i in 0..10) {
                                line.message("เริ่มจับเวลา ${pijob.name} ${df.format(Date())} ", token)
                                TimeUnit.SECONDS.sleep(1)
                            }
                        } else {
                            status = "ไม่สามารถ setport ได้"
                            if (token != null) {
                                line.message(
                                    "ไม่สามารถตั้งเวลาได้ !! ${pijob.name} ${df.format(Date())}  device name: ${it.device?.name} PORT:${it.portname?.name}",
                                    token
                                )
                            }

                            isRun = false
                        }
                        if (it.runtime != null && it.runtime!!.toInt() > maxrun) {
                            maxrun = it.runtime!!.toInt()
                        }
                        if (it.waittime != null && it.waittime!!.toInt() > maxwait) {
                            maxwait = it.waittime!!.toInt()
                        }

                    } catch (e: Exception) {
                        status = "ERROR in setport loop ${e.message}"
                        logger.error(e.message)
                    }
                }
            }
        } catch (e: Exception) {
            status = "Set port ERROR ${e.message}"
            logger.error("Set port ERROR ${e.message}")
            throw e
        }
    }

    //รจนกว่าจะถึงข้างบนถ้าถึงให้ทำการตั้งเวลาเลย
    fun waithightmp(p: Pijob): Boolean {
        try {
            var waittime = 10
            if (p.hlow != null) {
                waittime = p.hlow!!.toInt()
            }

            //ต้องเพิ่มการรอให้นานขึ้นกว่านี้
            for (i in 0..waittime) {

                try {
                    var t = mtp.readTmp(p, 1000)
                    status = "wait high tmp ${t}  count ${i}"
                    if (t != null && t.toDouble() >= p.thigh!!.toDouble()) {
                        status = "Tmp is ok ${t} check high "
                        //ให้ผ่านไปเลย เพราะจะมา function นี้ได้ต้องผ่าน low มาแล้ว

                        //ตรวจสอบอีกครังให้แน่ใจ
                        TimeUnit.SECONDS.sleep(5)
                        t = mtp.readTmp(p, 10000)
                        status = "Tmp is high ${t}"
                        if (t!!.toDouble() < p.thigh!!.toDouble()) {
                            status = "High tmp not sure ${t} ok exit"
                            return false //ไม่สูงจริง

                        }
                        status = "High ok run job"
                        return true
                    }
                } catch (e: Exception) {
                    logger.error("Wait hig tmp is ERROR ${e.message}")
                    throw e
                }
                TimeUnit.SECONDS.sleep(1)
            }
            status = "High time out TMP: ${waittime}"
//            throw Exception("Wait high timeout ${waittime}")
            return false
        } catch (e: Exception) {
            status = "Set high ERROR ${e.message}"
            logger.error("waithightmp ERROR ${e.message}")
            throw e
        }

    }


    //รอขันตำก่อนถ้าไม่ถึง 10 วิ หรือ ตาม hlow ถ้าถึงออกเลยแต่ถ้าไม่ถึงออก
    fun waitlowtmp(p: Pijob): Boolean {
        status = "Start wait low"
        try {
            var waittime = 10
            if (p.hlow != null) {
                waittime = p.hlow!!.toInt()
            }
            for (i in 0..waittime) {
                status = "Try to read Tmp"
                var t = mtp.readTmp(p, 10000)
                status = "Read low tmp ${t}"
                //จะต้องอยู่ในช่วงตำสุดของระบบแต่ไม่เกินสูงของระบบ
                if (t != null &&
                    (t.toDouble() >= p.tlow!!.toDouble() && t.toDouble() <= pijob.thigh!!.toDouble())
                ) {
                    return true
                }
                status = "Wait low time  Try:${i}  TMP:${t}"
                TimeUnit.SECONDS.sleep(1)
            }
            status = "Wait low timeout ${waittime}"
//            throw Exception("Wait low timeout ${waittime}")
            return false
        } catch (e: Exception) {
            status = "set low ERROR ${e.message}"
            logger.error("waitlowtmp ERROR ${e.message}")
            throw e
        }

    }


    var logger = LoggerFactory.getLogger(D1TimerII::class.java)
}