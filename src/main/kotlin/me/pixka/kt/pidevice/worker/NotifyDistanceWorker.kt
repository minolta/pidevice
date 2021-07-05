package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.c.Statusobj
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import java.util.*
import java.util.concurrent.TimeUnit

class NotifyDistanceWorker(
    p: Pijob, var mac: MactoipService,
    var target: String, var notify: NotifyService
) : DWK(p), Runnable {
    val om = ObjectMapper()

    override fun run() {
        try {
            startRun = Date()
            isRun = true
            status = "Status run"
            var result = mac.readStatus(pijob)
            var s = om.readValue<Statusobj>(result)
            var low = pijob.tlow!!.toInt()
            var high = pijob.thigh!!.toInt()
            var value = s.distance!!.toInt()

            if (value in low..high) {
                var token = System.getProperty("pressurenotify")//ใช้ token เดียวกับ pressure
                status = "Distination in rang   ${pijob.description}  ${low} < ${value} < ${high}"
                if (token != null) {
                    notify.message("Distination: ${pijob.description}  ${low} < ${value} < ${high} ", token)
                } else
                    notify.message("Distination: ${pijob.description}  ${low} < ${value} < ${high} ")
            } else {
                status = "Not in rang "
            }
            TimeUnit.SECONDS.sleep(1)

            exitdate = findExitdate(pijob)
            status = "End job normal"



        } catch (e: Exception) {

                status = "ERROR ${e.message}"
                isRun=false
        }

        status = "End job"
    }

}