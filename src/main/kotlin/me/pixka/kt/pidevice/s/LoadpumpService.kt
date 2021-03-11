package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.t.LoadPiJob
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LoadpumpService(val mtp: MactoipService,val pumpforpijobService: PumpforpijobService,
                      val pds:PideviceService,val pus:PumpforpijobService) {
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

    /**
    * รับปั็มใหม่เข้ามาในระบบแล้วค้นหาว่ามีหรือเปล่า
    * */
    fun resetPumps(pumps:List<Pumpforpijob>,job:Pijob)
    {

        var buf = ArrayList<Pumpforpijob>()
        var old = pus.bypijobid(job.id)

        if(old!=null)
        {
            old.forEach {
                  var refid = it.refid
                if(pumps.find { it.id == refid }==null)
                {
                    buf.add(it)//เพิ่มเข้าไป delete
                }
            }
        }


        logger.debug("Delete not use pumps in pijobs now ${buf.size}")

        buf.forEach {
            pus.delete(it)
        }
    }
    /**
     * บันทึกปั๊มใหม่ลงใน device
     */
    fun savePumps(pumps:List<Pumpforpijob>,job:Pijob)
    {
        if (pumps.size > 0) {//ถ้ามีข้อมูลปั๊ม

            pumps.forEach {

                var pi = pds.findByRefid(it.pidevice_id!!)
                if(pi==null)
                {
                    pi =  pds.findOrCreate(it.pidevice?.mac!!)
                    pi.ip = it.pidevice?.ip
                    pi.name = it.pidevice?.name
                    pds.save(pi)
                }
                var p = pus.checkPumpinjob(pi.id, job.id)
                if(p==null)
                {
                    p = Pumpforpijob()
                    p.pijob = job
                    p.pidevice = pi
                    p.enable = true
                    p.refid = it.id
                    println("New Pumps in job ")
                    pus.save(p)

                }
                else
                {
                    p.enable = it.enable
                    pus.save(p)
                }



            }

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