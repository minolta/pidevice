package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class DisplaytmpWorker(p: Pijob, val mtp: MactoipService) : DWK(p), Runnable {

    val om = ObjectMapper()
    override fun run() {
        startRun = Date()
        isRun = true
        status = "Start run"
        var display = true;
        var mac: String? = null
        var refid: Long? = 0L

        try {
            var t = mtp.readTmp(pijob,15000)
            if (pijob.tlow != null && t != null) //แสดงว่ามีการกำหนดอุณหภูมิขั้นตำสำหรับแสดงผล
            {
                val low = pijob.tlow?.toDouble()
                if (t.toDouble() < low!!) //ตำกว่ากำหนด
                    display=false
            }
            var name = pijob.desdevice?.name
            var ports = mtp.getPortstatus(pijob)

            if (t == null)
                throw Exception("T is NULL")
            if (ports == null)
                throw Exception("Ports is null")
//            if (t != null && ports != null) {

            if(display) {
                ports.filter { it.enable == true }.forEach {
                    val displayip = mtp.mactoip(it.device?.mac!!)
                    mac = it.device?.mac
                    refid = it.refid

//                    val name = it.device?.name
                    if (displayip != null) {
                        var l = 0
                        if (pijob.hlow != null) {
                            l = pijob.hlow?.toInt()!!
                        }
                        var url = mtp.http.encode("ความร้อน : ${name} = ${t}")
                        var u = "http://${displayip}/settext?t=${url}&tn=2&l=${l}"
                        status = u
                        var re = mtp.http.getNoCache(u, 10000)
                        status = "set Tmp ${name} ${t} ${re}"
                        exitdate = findExitdate(pijob)
                    } else {
                        mtp.lgs.createERROR(
                            "display ip no found", Date(), "DisplaytmpWorker",
                            "", "79", "run()", it.device?.mac
                        )
                        logger.error("Display ip not found")
                        status = "Display ip not found"
                        isRun = false
                    }
                }
            }

//            } else {
//                logger.error(" T:${t}   POST${ports}   ${pijob.name} ${pijob.desdevice?.mac}")
//                isRun = false
//            }
        } catch (e: Exception) {
            logger.error("Run() ${e.message}")
            status = "${e.message}"
            mtp.lgs.createERROR(
                "${e.message}", Date(),
                "DisplaytmpWork", "", "63", "run()", mac, refid
            )
            isRun = false
        }


        println("StartDATE: ${startRun} exitdate:${exitdate}")

//        status = "Job end wait for exit ${exitdate}"
        //ถ้าทุกอย่างปกติกให้ taskserver เป็น คน end เอง
    }


    var logger = LoggerFactory.getLogger(DisplaytmpWorker::class.java)

}