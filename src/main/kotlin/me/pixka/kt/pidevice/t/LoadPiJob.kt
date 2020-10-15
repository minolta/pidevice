package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * สำหรับ load pi job แบบใหม่ แบบเก่าไม่ใช้แล้ว
 */
@Component
@Profile("!test")
class LoadPiJob(val httpService: HttpService, val pjs: PijobService, val pds: PideviceService,
                val ptijs: PortstatusinjobService, val js: JobService, val pgs: PijobgroupService,
                val pns: PortnameService, val ls: LogistateService, val ips: IptableServicekt) {
    var host = System.getProperty("piserver")
    var target = host + "/pijob/lists"
    var targetloadstatus = host + "/portstatusinjob/lists"
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 2000)
    fun load() {
        var mac = System.getProperty("mac")
//        if (mac == null)
//            mac = io.wifiMacAddress()
        var re: String? = httpService.get(target + "/${mac}",500)
        var alljobs = om.readValue<List<Pijob>>(re!!)
        if (alljobs != null) {
            alljobs.forEach {

                var ref = pjs.findByRefid(it.id)
                if (ref != null) {
                    edit(it, ref)
                } else {
                    newjob(it)
                }

            }
        }
    }

    /**
     * ใช้สำหรับupdateip ที่ติดมากลับ pijob
     */
    fun updateIp(pd: PiDevice) {
        var ip = ips.findByMac(pd.mac!!)
        if (ip != null) {
            if (pd.ip != null) {
                ip.ip = pd.ip
                ips.save(ip)
            }

        } else {
            var i = Iptableskt()
            i.devicename = pd.name
            i.ip = pd.ip
            i.mac = pd.mac
            ips.save(i)
        }
    }

    /*
    * สำหรับค้นหา device ในเครื่องถ้าเจอแล้ว update ip ด้วย
    * */
    fun finddevice(device: PiDevice): PiDevice? {
        try {
            var d = pds.findOrCreate(device)
            if (d != null)
                updateIp(d)

            return d
        } catch (e: Exception) {
            logger.error("Find device ERROR ${e.message} ${device.name}")
            throw e
        }
    }
    /**
     * ใช้สำหรับ load port from server
     */
    fun loadPortinjob(refid: Long): List<Portstatusinjob>? {
        try {
            var re = httpService.get(targetloadstatus + "/" + refid,500)
            var ports = om.readValue<List<Portstatusinjob>>(re)
            return ports
        } catch (e: Exception) {
            logger.error("Load port status in job")
            throw e
        }
    }

    fun newjob(it: Pijob) {
        it.ds18sensor = null
        it.ds18sensor_id = null
        it.refid = it.id
        it.ports = null
        it.refverion = it.ver?.toLong() //เอาไว้สำหรับเทียบเวลา edit ถ้าไม่เท่ากันถึงจะ edit
        if (it.pijobgroup != null)
            it.pijobgroup = pgs.findOrCreate(it.pijobgroup?.name!!)
        if (it.job != null)
            it.job = js.findorcreate(it.job?.name!!)
        if (it.desdevice != null)
            it.desdevice = finddevice(it.desdevice!!)
        if (it.pidevice != null)
            it.pidevice = finddevice(it.pidevice!!)

        try {
            var newjob = pjs.save(it)

            if (newjob != null) {
                var ports = loadPortinjob(newjob.refid!!)
                if (ports != null) {
                    savenewPorts(newjob, ports)
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR Save new job ${it.name} ${e.message}")
            throw e
        }

    }

    fun savenewPorts(newjob: Pijob, ports: List<Portstatusinjob>) {
        ports.forEach {
            try {
                var pt = Portstatusinjob()
                pt.device = finddevice(it.device!!)
                pt.enable = it.enable
                pt.pijob = newjob
                pt.refid = it.id
                pt.portname = pns.findorcreate(it.portname?.name!!)
                pt.runtime = it.runtime
                pt.waittime = it.waittime
                pt.status = ls.findorcreate(it.status!!)
                pt.verref = it.ver
                ptijs.save(pt)
            } catch (e: Exception) {
                logger.error("Save port ${it.portname} ERROR ${e.message} JOB${newjob.name}")
            }
        }
    }

    /**
     * ใช้สำหรับ edit port
     */
    fun editPorts(job: Pijob, portsfromserver: List<Portstatusinjob>) {
        portsfromserver.forEach {
            try {

                var pt = ptijs.finByRefId(it.id) //ดึงจากในเครื่อง
                if (pt != null) { //old port
                    if(pt.verref!=it.ver) {
                        pt.device = finddevice(it.device!!)
                        pt.enable = it.enable
                        pt.pijob = job
                        pt.refid = it.id
                        pt.portname = pns.findorcreate(it.portname?.name!!)
                        pt.runtime = it.runtime
                        pt.waittime = it.waittime
                        pt.status = ls.findorcreate(it.status!!)
                        pt.verref = it.ver
                        ptijs.save(pt)
                    }
                } else //มาใหม่เพิ่ม
                {
                    var pt = Portstatusinjob()
                    pt.device = finddevice(it.device!!)
                    pt.enable = it.enable
                    pt.pijob = job
                    pt.refid = it.id
                    pt.portname = pns.findorcreate(it.portname?.name!!)
                    pt.runtime = it.runtime
                    pt.waittime = it.waittime
                    pt.verref = it.ver
                    pt.status = ls.findorcreate(it.status!!)
                    ptijs.save(pt)
                }
            } catch (e: Exception) {
                logger.error("Save port ${it.portname} ERROR ${e.message} JOB${job.name}")
            }
        }
    }

    fun edit(it: Pijob, ref: Pijob) {
        try {
            if (it.ver?.toInt() != ref.refverion?.toInt()) {
                ref.copy(it)
                if (ref.pijobgroup != null)
                    ref.pijobgroup = pgs.findOrCreate(it.pijobgroup?.name!!)
                if (ref.job != null)
                    ref.job = js.findorcreate(it.job?.name!!)
                if (ref.desdevice != null)
                    ref.desdevice = finddevice(it.desdevice!!)
                if (ref.pidevice != null)
                    ref.pidevice = finddevice(it.pidevice!!)
                pjs.save(ref)
            }

            var ports = loadPortinjob(ref.refid!!)
            if (ports != null && ports.size>0)
                editPorts(ref, ports as List<Portstatusinjob>)
        } catch (e: Exception) {
            logger.error("Edit pijob ${ref.name}  ERROR:${e.message}")
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LoadPiJob::class.java)
    }
}