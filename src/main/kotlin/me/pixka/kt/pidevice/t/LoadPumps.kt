package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.LoadpumpService
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LoadPumpsTask(
    val pjs: PijobService,
    val mtp: MactoipService,
    val pus: PumpforpijobService,
    val pds: PideviceService, val lps: LoadpumpService
) {

    var target = System.getProperty("piserver")

    @Scheduled(fixedDelay = 4000)
    fun loadpump() {
        try {
            var jobs = pjs.all()
            jobs.forEach {
                try {
                    var pumps = lps.loadPump(it.refid!!)
                    if (pumps.size > 0) {//ถ้ามีข้อมูลปั๊ม
                        lps.savePumps(pumps, it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            logger.error("Error load pumps ${e.message}")
        }
    }

    var logger = LoggerFactory.getLogger(LoadPumpsTask::class.java)

}