package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.o.HTObject
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.DWK
import org.slf4j.LoggerFactory
import java.util.*

class CheckHsensorWorker(job: Pijob, var mtp: MactoipService, val line: NotifyService) : DWK(job), Runnable  {
    val om = ObjectMapper()
    override fun run() {

        startRun= Date()
        isRun=true
        status = "Start run ${startRun}"
        try {
            var sensorstocheck = mtp.getPortstatus(pijob)
            if (sensorstocheck != null) {
                sensorstocheck.forEach {
                    if (it.enable == true) {
                        status = "Check h/t ${it.device?.name}"
                        var ip = mtp.mactoip(it.device?.mac!!)
                        var re = mtp.http.get("http://${ip}",60000)
                        var hobject = om.readValue<HTObject>(re)
                        status = "${it.device?.name} H:${hobject.h} T:${hobject.t}"
                        if(!hobject.equals(pijob))
                        {//ถ้าไม่อยู่ในช่วง ให้ notify
                            if(pijob.token!=null)
                            line.message("Device : ${it.device?.name} H/T out of rang TLOW: ${pijob.tlow}  THigh ${pijob.thigh}  T:${hobject.t} " +
                                    " HLOW :${pijob.hlow} HHIGH:${pijob.hhigh} ${hobject.h}",pijob.token!!)
                        }


                    }
                }
            } else {
                isRun = false
                exitdate = findExitdate(pijob)
            }
        }catch (e:Exception)
        {
            isRun=false
            logger.error("JOB ${pijob.name} ERROR ${e.message} ")
            throw e
        }

        exitdate = findExitdate(pijob)
        status = "End run"
    }




    var logger = LoggerFactory.getLogger(CheckHsensorWorker::class.java)
}