package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.*
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class Runloadpijob(val io: Piio, val service: PijobService, val psijs: PortstatusinjobService,
                   val ls: LogistateService, val pijobgroupService: PijobgroupService,
                   val js: JobService, val dsservice: DS18sensorService,
                   val ps: PortnameService, val pds: PideviceService) {
    val mapper = ObjectMapper()
    var ex = Executors.newSingleThreadExecutor()
    var host = System.getProperty("piserver")
    var target = host + "/pijob/lists"
    var targetloadstatus = host + "/portstatusinjob/lists"
    @Scheduled(fixedDelay = 3000)
    fun run() {
        try {
            var mac = System.getProperty("mac")
            if (mac == null)
                mac = io.wifiMacAddress()
            var list = loadPijob(mac)
            if (list != null) {
                save(list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error(e.message)
        }
    }


    fun save(list: List<Pijob>) {

        for (item in list) {

            var ref: Pijob? = null
            try {
                logger.debug("[loadpijob] Find Job Refs " + item)
                ref = service.findByRefid(item.id) // หาว่ามีในเครื่องเรายัง
                if (ref == null) {
                    // Save เข้าถ้าหาไม่เจอเป็น job ใหม่
                    logger.debug("[loadpijob] new Pi job on device ${item}")
                    try {
                        ref = newpijobinlocaldevice(item)
                    } catch (e: Exception) {
                        logger.error("new job in local error ${e.message}")
                    }
                } else {
                    try {
                        logger.debug("[loadpijob] Jobs already edit " + ref)
                        ref = edit(ref, item)
                    } catch (e: Exception) {
                        logger.error("Edit job error ${e.message}")
                    }

                }
            } catch (e: Exception) {
                logger.error("Save error ${e.message}")
                e.printStackTrace()
            }
            logger.debug("[loadpijob] Found ? ${ref}")



            saveportstatus(item, ref!!)
        }

    }

    fun loadPijob(mac: String): List<Pijob>? {
        val ee = Executors.newSingleThreadExecutor()
        try {
            var http = HttpGetTask(target + "/${mac}")
            logger.debug("${target}/${mac}")
            var f = ee.submit(http)
            var re: String? = null
            re = f.get(3, TimeUnit.SECONDS)
            logger.debug("Return ${re}")
            if (re != null) {
                val list = mapper.readValue<List<Pijob>>(re)
                logger.debug("[pijob loadpijob] Found Jobs for me " + list.size)
                return list
            }
           logger.warn("Not have pi job")
        } catch (e: IOException) {
            logger.error("[loadpijob] :error:" + e.message)
            ee.shutdownNow()
            throw e
        }

        return null
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

    fun newpijobgroup(pijobgroup: Pijobgroup): Pijobgroup? {
        var p = pijobgroupService.findByName(pijobgroup.name!!)
        if (p == null) {
            p = Pijobgroup(pijobgroup.name)
            p = pijobgroupService.save(p)
        }
        return p
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
            val dss = dsservice.findorcreate(dssensor.callname!!, dssensor.name!!)
            return dss
        } catch (e: Exception) {
            logger.error("loadpijob find ds sensor error ${e.message}")
            throw e
        }

    }

    fun newpijobinlocaldevice(item: Pijob): Pijob? {

        try {
            item.job = newjob(item.job!!)
            if (item.pijobgroup != null) {
                try {
                    var pijobgroup = newpijobgroup(item.pijobgroup!!)
                    item.pijobgroup = pijobgroup
                    item.pijobgroup_id = pijobgroup?.id
                } catch (e: Exception) {
                    logger.error("Create pijobgroup error ${e.message}")
                }
            }

            try {
                if (item.ds18sensor != null)
                    item.ds18sensor = newdssensor(item.ds18sensor!!)
            } catch (e: Exception) {
                logger.error("Can not create Sensor ${e.message}")
            }
            try {
                if(item.desdevice!=null) {
                    logger.debug(" ${item.name}  newotherdevice ${item.desdevice}")

                   var ot = newotherdevice(item.desdevice!!)
                    item.desdevice = ot
                    item.desdevice_id = ot?.id
                    logger.debug(" ${item.name} newotherdevice new desdevice ${item.desdevice}")
                }
            } catch (e: Exception) {
                logger.error(" ${item.name} Can not create otherdevice ${e.message}")

            }
            var p: Pijob = service.newpijob(item)
            p.copy(item)
            if (item.runwithid != null) {
                var rw = createLocalrunwith(item)
                if (rw != null)
                    p.runwithid = rw.id
            }
            logger.debug("Item for new pijob ${item}")

            logger.debug("P after newpijob ${p}")
            // ตัวของเราเอง

            p.pidevice = null
            p.pidevice_id = null
//            p.runwithid = item.runwithid
            p.name = item.name
            p.refid = item.id
            p.tlow = item.tlow
            p.thigh = item.thigh
            p.pijobgroup = item.pijobgroup
            p.job = item.job
            p.desdevice_id = item.desdevice_id
            p.desdevice = item.desdevice

            logger.debug("[loadpijob newforsave] new Pi job for save :" + p)
            p = service.save(p)!!
            logger.debug("[loadpijob pijobsaved] Save pi job to device : " + p)
            return p
        } catch (e: Exception) {
            logger.error("Can not create Pi job ${e.message}")
            throw e
        }
    }

    fun createLocalrunwith(item: Pijob): Pijob? {
        logger.debug("Create runwith")
        try {
            var runwithjob: Pijob? = null
            try {
                runwithjob = loadrunwith(item.runwithid!!)
            } catch (e: Exception) {
                logger.error("runwith load from server error ${e.message}")
            }

            if (runwithjob != null) {

                var local = service.findByRefid(runwithjob.id)
                logger.debug("local is ${local} runwith")
                if(local==null) {
                    logger.debug("foundrunwith ==> ${runwithjob}")
                    var rw = Pijob()
                    rw.copy(runwithjob)
                    rw.refid = runwithjob.id
                    rw.pidevice_id = null
                    rw.pidevice = null
                    if (runwithjob.pijobgroup != null) {
                        var pijobgroup = newpijobgroup(runwithjob.pijobgroup!!)
                        rw.pijobgroup = pijobgroup
                        rw.pijobgroup_id = pijobgroup?.id
                    }
                    try {
                        if (runwithjob.ds18sensor != null)
                            rw.ds18sensor = newdssensor(runwithjob.ds18sensor!!)
                    } catch (e: Exception) {
                        logger.error("Can not create Sensor ${e.message}")
                    }

                    if (runwithjob.desdevice != null) {
                        try {
                            rw.desdevice = newotherdevice(runwithjob.desdevice!!)
                        } catch (e: Exception) {
                            logger.error("Can not create Other Device ${e.message}")

                        }
                    }
                    logger.debug("To save runwith ${rw}")
                    var localrunwith = service.save(rw)
                    logger.debug("local runwith ${localrunwith}")
                    return localrunwith
                }
                else
                {
                    logger.error("Please edit  runwith job")

                }

            }
            return null
        } catch (e: Exception) {
            logger.error("runwith EEE ${e.message}")
            throw e
        }

    }

    fun loadrunwith(id: Long): Pijob {

        val ee = Executors.newSingleThreadExecutor()
        try {
            val url = "${host}/rest/pijob/get/${id}"
            var http = HttpGetTask(url)
            var f = ee.submit(http)
            var re = f.get(5, TimeUnit.SECONDS)
            var found = mapper.readValue(re, Pijob::class.java)

            logger.debug("Found job runwith ${found}")
            return found

        } catch (e: Exception) {
            logger.error("load error runwith ${e.message}")
            throw  e
        }

    }

    private fun saveportstatus(item: Pijob, ref: Pijob) {
        logger.debug("[saveportstatus] Save port status ")
        try {
            val listofports = loadPortstate(item.id)
            saveport(listofports!!, ref)
        } catch (e: Exception) {
            logger.error("[saveportstatus] Save port error: " + e.message)
        }

    }

    fun loadPortstate(pijobid: Long?): List<Portstatusinjob>? {
        try {
            var http = HttpGetTask(targetloadstatus + "/" + pijobid)
            var f = ex.submit(http)
            var re = f.get(3, TimeUnit.SECONDS)
            if (re != null) {

                val list = mapper.readValue<List<Portstatusinjob>>(re)
                logger.debug("[saveportstatus] Found Port states  for me  ***************************************")
                logger.debug(" saveportstatus ${list}")
                logger.debug("******************************************************************************")
                return list
            }

            throw Exception(" saveportstatus Not found  port status")
        } catch (e: IOException) {
            logger.error("[saveportstatus] Load port status : " + e.message)
            throw e
        }
    }

    /**
     * Edit pijob
     */
    fun edit(ref: Pijob, item: Pijob): Pijob? {
        if (ref.verref == item.ver) {
            return ref //same no need edit
        }
        try {
            logger.debug("editpijob")
            ref.copy(item)

            logger.debug("Ds18sensor ${item.ds18sensor}")
            val ic = item.ds18sensor
            if (ic != null)
                ref.ds18sensor = dsservice.findorcreate(ic)
            var dd = item.desdevice
            if (dd != null) {
                ref.desdevice = newotherdevice(dd)
            }
            if (item.pijobgroup != null) {
                var pg = pijobgroupService.findByName(item.pijobgroup!!.name!!)
                if (pg == null) {
                    pg = Pijobgroup()
                    pg.name = item.pijobgroup?.name
                    ref.pijobgroup = pijobgroupService.save(pg)
                } else
                    ref.pijobgroup = pg

            }

            return service.save(ref)
        } catch (e: Exception) {
            logger.error("edit job  ${ref.name} ")
            throw e
        }

        return null
    }

    fun find(pn: Portname): Portname? {
        try {
            logger.debug("[loadpijob saveport Portname to save] " + pn)
            var p = ps.findorcreate(pn.name!!)
            logger.debug("[loadpijob saveport Portname found] " + p)
            return p
        } catch (e: Exception) {
            logger.error("Find port name ${e.message}")
            throw e
        }

    }

    fun find(l: Logistate): Logistate? {
        try {
            logger.debug("[loadpijob saveport Logistatus to save] " + l)
            val lg = ls.findorcreate(l.name!!) // find
            logger.debug("[loadpijob saveport Logi found] " + lg)
            return lg
        } catch (e: Exception) {
            logger.error("Find logic  ${e.message}")
            throw e
        }
    }

    fun saveport(listofports: List<Portstatusinjob>, pi: Pijob) {

        try {
            for (item in listofports) {
                logger.debug("Item ${item} ver:${item.ver} ")
                var ref: Portstatusinjob? = psijs.finByRefId(item.id)
                if (ref == null) {
                    logger.debug("1 [loadpijob saveport] new portstatus  ${item.device}")
                    var pn = item.portname
                    pn = find(pn!!)
                    item.portname = pn
                    item.pijob = pi
                    item.refid = item.id
                    val lg = find(item.status!!)
                    logger.debug("2 [loadpijob  saveport status  to save] " + pn)
                    item.status = lg
                    if (item.device != null) {
                        logger.debug(" 3 find Device ${item.device}")
                        item.device = newotherdevice(item.device!!)


                    } else {
                        logger.debug("**************** ${item}")
                    }

                    psijs.save(item)
                    logger.debug("[ 4 loadpijob saveport] Save port :" + item)
                } else {
                    logger.debug("[5 loadpijob saveport] have portsttus already REF:${ref} ${ref.verref} != ${item.ver}")
                    if (ref.verref != item.ver)
                        ref = editport(ref, item)
                }
            }
        } catch (e: Exception) {
            logger.error("[ 6 loadpijob saveport] saveport error : " + e.message)
            throw e
        }

    }

    fun editport(ref: Portstatusinjob, item: Portstatusinjob): Portstatusinjob? {
        try {
            logger.debug("[loadpijob] editport  :" + item)
            ref.copy(item)
            ref.portname = ps.findorcreate(item.portname?.name!!)
            ref.status = ls.findorcreate(item.status?.name!!)
            ref.enable = item.enable
            ref.runtime = item.runtime
            ref.waittime = item.waittime
            ref.enable = item.enable
            if (item.device != null)
                ref.device = newotherdevice(item.device!!)


            return psijs.save(ref)

        } catch (e: Exception) {
            logger.error("loadpijob editport error : " + e.message)
            throw e
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runloadpijob::class.java)
    }
}