package me.pixka.pibase.s

import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Pijob
import me.pixka.pibase.r.Ds18sensorRepo
import me.pixka.pibase.r.PijobRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@Service
class PijobService( var r: PijobRepo, val dss: Ds18sensorRepo) : Ds<Pijob>() {
    override fun search(search: String, page: Long, limit: Long): List<Pijob>? {
        return r.search(search, topage(page, limit))
    }

    fun findJob(jobid:Long):List<Pijob>?
    {
        return r.findByJob_idAndEnableOrderByPriority(jobid,true)
    }
    fun findByTime(currenttime: Date, jobid: Long): List<Pijob>? {
        var c = datetoLong(currenttime)
        return r.fineByTime(c, jobid)
    }

    fun search(search: String, uid: Long, page: Long, limit: Long): List<Pijob>? {
        return r.search(search, uid, topage(page, limit))
    }

    fun findByName(n: String): Pijob? {
        return r.findByName(n)
    }

    fun findByName(n: String, uid: Long): Pijob? {
        return r.findByNameAndAddby(n, uid)
    }

    fun searchMatch(n: String): Pijob? {
        return null
    }

    fun findByRefid(id: Long?): Pijob? {
        return r.findByRefid(id)
    }

    fun findByCounter(id: Long): List<Pijob>? {
        return r.findByJob_id(id)
    }

    fun findByPidevice_id(id: Long?): List<*>? {
        return r.findByPidevice_id(id)
    }

    fun findByHT(h: BigDecimal, t: BigDecimal, jobid: Long?): List<*>? {
        return r.findByHT(h, t, jobid)
    }

    fun findByDS(t: BigDecimal, jobid: Long?): List<Pijob>? {
        return r.findByDS(t, jobid, true) // ต้องเอาแต่ enable เท่านั้น
    }

    fun findByH(h: BigDecimal, jobid: Long?): List<Pijob>? {
        return r.findByH(h, jobid, true)
    }

    fun findByT(t: BigDecimal, jobid: Long?): List<Pijob>? {
        return r.findByT(t, jobid, true)
    }

    fun findAllByT(page: Long?, limit: Long?): List<*>? {
        return r.fintAllOrderByT(this.topage(page!!, limit!!))
    }

    /**
     * ใช้ค้นหาจาช่วงเวลา เช่นค้นหา งานที่ต้องทำในช่วง 2.00am - 4.00am
     * อุณหภูมิในช่วง 40
     *
     * @param t
     * @param time
     * @param jobid
     * @return
     */
    fun findByDSBytime(t: BigDecimal, time: Date, jobid: Long?): List<*>? {
        var time = time

        val df = SimpleDateFormat("HH:mm:ss")
        val s = df.format(time)

        try {
            time = df.parse(s)
            logger.debug("[ds18b20 finbydstime] Time to search " + time)
            val list = r.findByDS(t, time, jobid)
            if(list!=null)
            logger.debug("[ds18b20 finbydstime] Job founds " + list.size)
            return list
        } catch (e: ParseException) {
            logger.error("[ds18b20 finbydstime] " + e.message)
            e.printStackTrace()
        }

        return null
    }


    fun findByHBytime(h: BigDecimal, time: Long, jobid: Long?): List<Pijob>? {


        try {

            logger.debug("[ds18b20 finbyHbytimetime Hbytime] Time to search " + time)
            val list = r.findByHByTime(h, jobid!!, true, time)
            logger.debug("[ds18b20 finbydstime Hbytime] Job founds " + list!!.size)
            return list
        } catch (e: Exception) {
            logger.error("[ds18b20 finbydstime Hbytime] " + e.message)
            e.printStackTrace()
        }

        return null
    }


    fun clear() {
        repo.deleteAll()
    }

    /**
     * ใช้สำหรับค้นหา PI job ที่อ่านค่าจาก device ตัวอื่น
     */
    fun findDSOTHERJob(jobid: Long): ArrayList<Pijob>? {
        return r.findDSOther(jobid)
    }

    /**
     * Save own job in local device
     */
    fun newpijob(item: Pijob): Pijob {

        try {
            val p = Pijob()
            p.copy(item)
            return p
        } catch (e: Exception) {
            logger.error("Canon add pijob")
            e.printStackTrace()
            throw e
        }

    }

    fun findByDSDP(id: Long?): List<*>? {
        try {

            // ใช้หา pi job ที่เป็น DSDP
            val list = r.findByJob_id(id)
            logger.debug("Findbydsdp Founds " + list?.size)
            return list
        } catch (e: Exception) {
            logger.error("Findbydsdp ERROR: " + e.message)
        }

        return null
    }

    fun searchByDeviceid(id: Long?, page: Long?, limit: Long?): List<Pijob>? {

        return r.searchByDeviceid(id, this.topage(page!!, limit!!))
    }

    fun findOnecommand(): List<Pijob>? {
        return r.findByOne(true)
    }


    /**
     * ใช้สำหรับหา งานที่ต้องทำสำหรับ pi
     * โดยระบุ อุณหภูมิ t
     * Sensor จาก sensorid
     * job จาก dsjobid
     */

    fun findDSJOBBySensor(t: BigDecimal, sensorid: Long, dsjobid: Long): ArrayList<Pijob>? {

        return r.DSBysensor(dsjobid, sensorid, t)
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(PijobService::class.java)
    }

    val d = SimpleDateFormat("yyyy/mm/dd HH:mm")
    val df = SimpleDateFormat("HH:mm")
    fun timeTolong(t: String): Long {
        var n = "1970/1/1 " + t

        var d = d.parse(n)
        return d.time

    }

    fun deleteById(id: Long): Boolean {
        r.deletePijobById(id)
        return true
    }

    fun datetoLong(d: Date): Long {
        var s = df.format(d)
        return timeTolong(s)
    }
}
