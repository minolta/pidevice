package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.*
import me.pixka.pibase.s.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
//ไม่ใช้แล้ว
@Component
@Profile("pi", "lite")
class Loadpijob(val task: LoadpijobTask) {

    //@Scheduled(initialDelay = 60000, fixedDelay = 60000)
    fun run() {
        logger.info("Start load pijob")
        var f = task.run()
        var count = 0
        while (true) {
            if (f!!.isDone) {
                logger.info("Load pi job end")
                break
            }

            TimeUnit.SECONDS.sleep(1)
            count++
            if (count > 30) {
                logger.error("Time out")
                f.cancel(true)
            }
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Loadpijob::class.java)
    }

}

@Component
@Profile("pi", "lite")
class LoadpijobTask(val service: PijobService, val dsservice: DS18sensorService, val dbcfg: DbconfigService
                    , val io: Piio, val http: HttpControl,
                    val psijs: PortstatusinjobService, val ls: LogistateService,
                    var err: ErrorlogService, val js: JobService,
                    val ps: PortnameService, val pds: PideviceService) {
    // load /pijob/list/{mac}
    private var target = ""
    // load port state /portstate/list/{mac}
    private var targetloadstatus = ""


    @Async("aa")
    fun run(): Future<Boolean>? {
        try {

            setup()
            val list = loadPijob(io.wifiMacAddress())

            if (list != null) {
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
            return AsyncResult(true)
        } catch (e: Exception) {
            logger.error(" [loadpijob] Error run load pijob : " + e.message)
            err.n("loadpijob", "40-90", "${e.message}")
        }
        return null
    }

    fun newjob(job: Job): Job? {
        try {
            val job = js.findandcreateLocal(job)
            return job
        } catch (e: Exception) {
            logger.error("loadpijob find job error ${e.message}")
        }

        return null
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
            err.n("loadpijob", "60-61", "${e.message}")
        }
        return null
    }

    fun newdssensor(dssensor: DS18sensor): DS18sensor? {

        try {
            val dss = dsservice.findorcreate(dssensor)
            return dss
        } catch (e: Exception) {
            logger.error("loadpijob find ds sensor error ${e.message}")
            //   err.n("loadpijob", "60-61", "${e.message}")
        }

        return null
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
            }

            logger.debug("Item for new pijob ${item}")


            var p: Pijob = service.newpijob(item)


            logger.debug("P after newpijob ${p}")
            // ตัวของเราเอง
            p.pidevice = null
            p.pidevice_id = null
            logger.debug("[loadpijob newforsave] new Pi job for save :" + p)
            p = service.save(p)!!
            logger.debug("[loadpijob pijobsaved] Save pi job to device : " + p)
            return p
        } catch (e: Exception) {
            logger.error("Can not create Pi job ${e.message}")
            e.printStackTrace()
        }
        return null
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

    private fun edit(ref: Pijob, item: Pijob): Pijob? {
        try {
            ref.copy(item)
            val ic = item.ds18sensor
            if (ic != null)
                ref.ds18sensor = dsservice.findorcreate(ic)
            var dd = item.desdevice
            if(dd!=null)
            {
                var dds = pds.findByMac(dd.mac!!)
                if(dds==null)
                    dds = pds.create(dd.mac!!,dd.mac!!)
                ref.desdevice = dds
            }
            return service.save(ref)
        } catch (e: Exception) {
            logger.error("edit job  ${ref.name} ")
            throw e
        }

        return null
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
            err.n("loadpijob", "131-151", "${e.message}")
        }

    }

    fun find(l: Logistate): Logistate? {
        logger.debug("[loadpijob saveport Logistatus to save] " + l)
        val lg = ls.findorcreate(l.name!!) // find
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
            ref.portname = ps.findorcreate(item.portname?.name!!)
            ref.status = ls.findorcreate(item.status?.name!!)
            ref.enable = item.enable
            return psijs.save(ref)
        } catch (e: Exception) {
            logger.error("loadpijob edit error : " + e.message)
            err.n("loadpijob", "176-183", "${e.message}")
        }

        return null
    }


    fun loadPortstate(pijobid: Long?): List<Portstatusinjob>? {


        try {
            val mapper = ObjectMapper()
            val re = http.get(targetloadstatus + "/" + pijobid)
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
            val mapper = ObjectMapper()
            val re = http.get(target + "/" + mac)
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

        var host = System.getProperty("piserver")

        if (host == null)
            host = dbcfg.findorcreate("hosttarget", "http://pi1.pixka.me").value

        target = host + "/pijob/lists"
        targetloadstatus = host + "/portstatusinjob/lists"
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(LoadpijobTask::class.java)
    }

}

