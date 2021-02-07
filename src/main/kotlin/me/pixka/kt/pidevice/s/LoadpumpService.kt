package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pidevice.t.LoadPiJob
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LoadpumpService(val mtp: MactoipService,val pumpforpijobService: PumpforpijobService) {
val om = ObjectMapper()
    fun loadPump(pijobid: Long): List<Pumpforpijob> {
        try {
            var ps = System.getProperty("piserver")
            var re = mtp.http.get("${ps}/pump/${pijobid}")
            var list = mtp.om.readValue<List<Pumpforpijob>>(re)
            return list
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
            throw e
        }
    }

    fun loadPump(pijobid: Long,url:String): List<Pumpforpijob> {
        try {
            var re = mtp.http.get("${url}${pijobid}",60000)
            var list = om.readValue<List<Pumpforpijob>>(re)
            return list
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
            throw e
        }
    }
    fun indevice(pijobid: Long): List<Pumpforpijob>? {
        try{
            return pumpforpijobService.bypijobid(pijobid)
        }catch (e:Exception)
        {
            logger.error("indevice ERROR ${e.message}")
            throw e
        }
    }
    var logger = LoggerFactory.getLogger(LoadPiJob::class.java)
}