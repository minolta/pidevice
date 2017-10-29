package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.pibase.d.Logistate
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.d.Portname
import me.pixka.pibase.d.Portstatusinjob
import me.pixka.pibase.s.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class Loadpijob(val service: PijobService, val dsservice: DS18sensorService, val dbcfg: DbconfigService
                , val io: Piio, val http: HttpControl, val psijs: PortstatusinjobService, val ls: LogistateService,
                var err: ErrorlogService, val js: JobService, val ps: PortnameService) {
    // load /pijob/list/{mac}
    private var target = ""
    // load port state /portstate/list/{mac}
    private var targetloadstatus = ""

    val mapper = jacksonObjectMapper()
    @Scheduled(fixedDelay = 60000)
    fun run() {

        logger.info("Start load pijob")
        setup()

        try {
            val list = loadPijob(io.wifiMacAddress())

            for (item in list!!) {
                logger.debug("[loadpijob] Find Job Refs " + item)
                var ref: Pijob? = service.findByRefid(item.id) // หาว่ามีในเครื่องเรายัง

                logger.debug("[loadpijob] Found ? ${ref}")

                if (ref == null) {// Save เข้าถ้าหาไม่เจอเป็น job ใหม่
                    logger.debug("[loadpijob] new Pi job on device ${item}")

                    try {
                        val job = js.findandcreateLocal(item.job!!)
                        item.job = job

                    } catch (e: Exception) {
                        logger.error("loadpijob find job error ${e.message}")
                        err.n("loadpijob", "52-53", "${e.message}")
                    }
                    try {
                        val dss = dsservice.findorcreate(item.ds18sensor)
                        item.ds18sensor = dss
                    } catch (e: Exception) {
                        logger.error("loadpijob find ds sensor error ${e.message}")
                        err.n("loadpijob", "60-61", "${e.message}")
                    }

                    var p: Pijob? = null;
                    try {
                        p = service.newpijob(item)
                    } catch (e: Exception) {
                        logger.error("loadpijob new pijob  error ${e.message}")
                        err.n("loadpijob", "69", "${e.message}")
                    }
                    p?.pidevice = null
                    p?.pidevice_id = null
                    logger.debug("[loadpijob newforsave] new Pi job for save :" + p)
                    p = service.save(p!!)
                    logger.debug("[loadpijob pijobsaved] Save pi job to device : " + p)
                    ref = p!!
                } else {
                    logger.debug("[loadpijob] Jobs already edit " + ref)
                    ref = edit(ref, item)

                }
                saveportstatus(item, ref!!)
            }

        } catch (e: Exception) {
            logger.error(" [loadpijob] Error run load pijob : " + e.message)
            err.n("loadpijob", "40-90", "${e.message}")
        }


    }


    private fun saveportstatus(item: Pijob, ref: Pijob) {
        logger.debug("[loadpijob] Save port status ")
        try {
            val listofports = loadPortstate(item.id)
            saveport(listofports!!, ref)
        } catch (e: Exception) {
            logger.error("[loadpijob] Save port error: " + e.message)
            err.n("loadpijob", "102-103", "${e.message}")
        }

    }

    private fun edit(ref: Pijob, item: Pijob): Pijob? {
        try {
            ref.copy(item)
            val ic = item.ds18sensor
            if (ic != null)
                ref.ds18sensor = dsservice.findorcreate(ic)
            return service.save(ref)
        } catch (e: Exception) {
            logger.error("[loadpijob edit error]" + e.message)
            err.n("loadpijob", "113-117", "${e.message}")
        }

        return null
    }


    private fun saveport(listofports: List<Portstatusinjob>, pi: Pijob) {

        try {
            for (item in listofports) {
                var ref: Portstatusinjob? = psijs!!.finByRefId(item.id)
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
            err.n("loadpijob", "131-151", "${e.message}")
        }

    }

    fun find(l: Logistate): Logistate? {
        logger.debug("[loadpijob saveport Logistatus to save] " + l)
        val lg = ls!!.findorcreate(l.name!!) // find
        logger.debug("[loadpijob saveport Logi found] " + lg)
        return lg

    }

    fun find(pn: Portname): Portname {
        var pn = pn
        logger.debug("[loadpijob saveport Portname to save] " + pn)
        pn = ps.findorcreate(pn.name!!)!! // find local port
        logger.debug("[loadpijob saveport Portname found] " + pn)
        return pn
    }

    fun editport(ref: Portstatusinjob, item: Portstatusinjob): Portstatusinjob? {
        try {
            logger.debug("[loadpijob] edit  :" + item)

            ref.copy(item)
            ref.portname = ps!!.findorcreate(item.portname?.name!!)
            ref.status = ls!!.findorcreate(item.status?.name!!)
            return psijs!!.save(ref)
        } catch (e: Exception) {
            logger.error("loadpijob edit error : " + e.message)
            err.n("loadpijob", "176-183", "${e.message}")
        }

        return null
    }


    fun loadPortstate(pijobid: Long?): List<Portstatusinjob>? {


        try {
            val re = http!!.get(targetloadstatus + "/" + pijobid)
            val list = mapper.readValue<List<Portstatusinjob>>(re)
            logger.debug("[loadpijob] Found Port states  for me " + list.size)
            return list
        } catch (e: IOException) {
            logger.error("[loadpijob] Load port status : " + e.message)
            err.n("loadpijob", "196-199", "${e.message}")
        }

        return null
    }


    fun loadPijob(mac: String): List<Pijob>? {

        try {
            val re = http!!.get(target + "/" + mac)
            val list = mapper.readValue<List<Pijob>>(re)
            logger.debug("[pijob loadpijob] Found Jobs for me " + list.size)
            return list
        } catch (e: IOException) {
            logger.error("[loadpijob] :error:" + e.message)
            err.n("loadpijob", "212-215", "${e.message}")
            return null
        }
    }

    fun setup() {
        target = dbcfg.findorcreate("serverloadpijob", "http://pi.pixka.me:5000/pijob/lists").value!!
        targetloadstatus = dbcfg.findorcreate("serverloadpijobportstate", "http://pi.pixka.me:5000/portstatusinjob/lists").value!!
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Loadpijob::class.java)
    }

}

