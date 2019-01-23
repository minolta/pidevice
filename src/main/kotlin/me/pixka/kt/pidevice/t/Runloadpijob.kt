package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.pibase.s.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class Runloadpijob(val io: Piio, val service: PijobService, val psijs: PortstatusinjobService, val ls: LogistateService,
                   var err: ErrorlogService, val js: JobService, val dsservice: DS18sensorService,
                   val ps: PortnameService, val pds: PideviceService) {
    val mapper = ObjectMapper()
    var ex = Executors.newSingleThreadExecutor()
    var host = System.getProperty("piserver")
    var target = host + "/pijob/lists"
    var targetloadstatus = host + "/portstatusinjob/lists"
    @Scheduled(fixedDelay = 3000)
    fun run() {
        try {
            var list = loadPijob(io.wifiMacAddress())
            if (list != null) {
                save(list)
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }


    fun save(list: List<Pijob>) {

        for (item in list) {

            var ref: Pijob? = null
            try {
                logger.debug("[loadpijob] Find Job Refs " + item)
                ref = service.findByRefid(item.id) // หาว่ามีในเครื่องเรายัง
            } catch (e: Exception) {
                logger.error(e.message)
            }
            logger.debug("[loadpijob] Found ? ${ref}")

            if (ref == null) {
                // Save เข้าถ้าหาไม่เจอเป็น job ใหม่
                logger.debug("[loadpijob] new Pi job on device ${item}")
                ref = newpijobinlocaldevice(item)
            } else {

                logger.debug("[loadpijob] Jobs already edit " + ref)
                ref = edit(ref, item)
            }
            saveportstatus(item, ref!!)
        }

    }

    fun loadPijob(mac: String): List<Pijob>? {

        try {
            var http = HttpGetTask(target + "/${mac}")
            var f = ex.submit(http)
            var re: String? = null
            re = f.get(3, TimeUnit.SECONDS)
            logger.debug("Return ${re}")
            if (re != null) {
                val list = mapper.readValue<List<Pijob>>(re)
                logger.debug("[pijob loadpijob] Found Jobs for me " + list.size)
                return list
            }
            throw Exception("Not have pi job")
        } catch (e: IOException) {
            logger.error("[loadpijob] :error:" + e.message)
            throw e
        }
    }

    fun newjob(job: Job): Job? {
        try {
            val job = js.findandcreateLocal(job)
            return job
        } catch (e: Exception) {
            logger.error("loadpijob find job error ${e.message}")
            throw e
        }

    }

    fun newotherdevice(pd: PiDevice): PiDevice? {

        try {

            var other = pds.findByMac(pd.mac!!)
            logger.debug("New Other device ${pd} Have already in device")
            if (other == null) {

                var p = pds.create(pd.mac!!, pd.id)
                logger.debug("not found in this device create new ${pd} new obj ${p}")
                return p
            }

            return other

        } catch (e: Exception) {
            logger.error("loadpijob find ds sensor error ${e.message}")
            throw e
        }
        return null
    }

    fun newdssensor(dssensor: DS18sensor): DS18sensor? {

        try {
            val dss = dsservice.findorcreate(dssensor)
            return dss
        } catch (e: Exception) {
            logger.error("loadpijob find ds sensor error ${e.message}")
            throw e
        }

    }

    private fun newpijobinlocaldevice(item: Pijob): Pijob? {

        try {
            item.job = newjob(item.job!!)
            try {
                item.ds18sensor = newdssensor(item.ds18sensor!!)
            } catch (e: Exception) {
                logger.error("Can not create Sensor")
            }
            try {
                item.desdevice = newotherdevice(item.desdevice!!)
            } catch (e: Exception) {
                logger.error("Can not create Other Device")
                throw e
            }
            logger.debug("Item for new pijob ${item}")
            var p: Pijob = service.newpijob(item)
            logger.debug("P after newpijob ${p}")
            // ตัวของเราเอง
            p.pidevice = null
            p.pidevice_id = null
            p.runwithid = item.runwithid
            logger.debug("[loadpijob newforsave] new Pi job for save :" + p)
            p = service.save(p)!!
            logger.debug("[loadpijob pijobsaved] Save pi job to device : " + p)
            return p
        } catch (e: Exception) {
            logger.error("Can not create Pi job ${e.message}")
            throw e
        }
    }


    private fun saveportstatus(item: Pijob, ref: Pijob) {
        logger.debug("[loadpijob] Save port status ")
        try {
            val listofports = loadPortstate(item.id)
            saveport(listofports!!, ref)
        } catch (e: Exception) {
            logger.error("[loadpijob] Save port error: " + e.message)
        }

    }

    fun loadPortstate(pijobid: Long?): List<Portstatusinjob>? {
        try {
            var http = HttpGetTask(targetloadstatus + "/" + pijobid)
            var f = ex.submit(http)
            var re = f.get(3, TimeUnit.SECONDS)
            if (re != null) {

                val list = mapper.readValue<List<Portstatusinjob>>(re)
                logger.debug("[loadpijob] Found Port states  for me " + list.size)
                return list
            }

            throw Exception("Not found  port status")
        } catch (e: IOException) {
            logger.error("[loadpijob] Load port status : " + e.message)
            throw e
        }
    }

    private fun edit(ref: Pijob, item: Pijob): Pijob? {
        try {
            ref.copy(item)
            val ic = item.ds18sensor
            if (ic != null)
                ref.ds18sensor = dsservice.findorcreate(ic)
            var dd = item.desdevice
            if (dd != null) {
                var dds = pds.findByMac(dd.mac!!)
                if (dds == null)
                    dds = pds.create(dd.mac!!, dd.mac!!)
                ref.desdevice = dds
            }
            return service.save(ref)
        } catch (e: Exception) {
            logger.error("edit job  ${ref.name} ")
            throw e
        }

        return null
    }

    fun find(pn: Portname): Portname {

        logger.debug("[loadpijob saveport Portname to save] " + pn)
        var p = ps.findorcreate(pn.name!!)
        logger.debug("[loadpijob saveport Portname found] " + pn)
        return pn
    }

    fun find(l: Logistate): Logistate? {
        logger.debug("[loadpijob saveport Logistatus to save] " + l)
        val lg = ls.findorcreate(l.name!!) // find
        logger.debug("[loadpijob saveport Logi found] " + lg)
        return lg

    }

    private fun saveport(listofports: List<Portstatusinjob>, pi: Pijob) {

        try {
            for (item in listofports) {
                var ref: Portstatusinjob? = psijs.finByRefId(item.id)
                if (ref == null) {
                    logger.debug("[loadpijob saveport] new portstatus ")
                    var pn = item.portname
                    pn = find(pn!!)

                    item.portname = pn
                    item.pijob = pi

                    item.refid = item.id
                    val lg = find(item.status!!)
                    logger.debug("[loadpijob  saveport status  to save] " + pn)
                    item.status = lg

                    psijs.save(item)
                    logger.debug("[loadpijob saveport] Save port :" + item)
                } else {
                    logger.debug("[loadpijob saveport] have portsttus already " + ref)
                    ref = editport(ref, item)
                }
            }
        } catch (e: Exception) {
            logger.error("[loadpijob saveport] saveport error : " + e.message)
            throw e
        }

    }

    fun editport(ref: Portstatusinjob, item: Portstatusinjob): Portstatusinjob? {
        try {
            logger.debug("[loadpijob] edit  :" + item)

            ref.copy(item)
            ref.portname = ps.findorcreate(item.portname?.name!!)
            ref.status = ls.findorcreate(item.status?.name!!)
            ref.enable = item.enable
            return psijs.save(ref)
        } catch (e: Exception) {
            logger.error("loadpijob edit error : " + e.message)
            throw e
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runloadpijob::class.java)
    }
}