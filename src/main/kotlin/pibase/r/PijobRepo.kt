package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import me.pixka.kt.pibase.d.Pijob
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*
import javax.transaction.Transactional

@Repository
interface PijobRepo : JpaRepository<Pijob, Long>, search<Pijob>, findByName<Pijob> {

    fun findByRefid(id: Long?): Pijob?

    fun findByPidevice_id(id: Long?): List<*>?

    @Query("from Pijob pj where (pj.hlow <= ?1 and pj.hhigh >=?1) and (pj.tlow <= ?2 and pj.thigh >=?2)  and pj.job_id = ?3 and (pj.stime is null and pj.etime is null)")
    fun findByHT(h: BigDecimal, t: BigDecimal, id: Long?): List<*>?

    /**
     * สำหรับ ค้นหา แต่ค่า h อย่างเดียว
     *
     * @param h
     * @param id
     * @param b
     * @return
     */
    @Query("from Pijob pj where (pj.hlow <= ?1 and pj.hhigh >=?1) and pj.job_id = ?2 and (pj.stime is null and pj.etime is null) and (pj.sdate is null and pj.edate is null)  and pj.enable = ?3")
    fun findByH(h: BigDecimal, id: Long?, b: Boolean): List<Pijob>?


    /**
     * ใช้สำหรับค้นหา H job ในช่วงเวลา  เช่น 10:00 - 11:00
     */
    @Query("from Pijob pj where (pj.hlow <= ?1 and pj.hhigh >=?1) and pj.job_id = ?2 and (pj.lowtime <= ?4 and pj.hightime >= ?4) and (pj.sdate is null and pj.edate is null)  and pj.enable = ?3")
    fun findByHByTime(h: BigDecimal, jobid: Long, enable: Boolean, time: Long): List<Pijob>?

    @Query("from Pijob pj where (pj.tlow <= ?1 and pj.thigh >=?1)  and pj.job_id = ?2 and (pj.stime is null and pj.etime is null) and pj.enable = ?3")
    fun findByT(t: BigDecimal, jobtypeid: Long?, b: Boolean): List<Pijob>?

    @Query("from Pijob pj where (pj.tlow <= ?1 and pj.thigh >=?1) and pj.job_id = ?2" + " and (pj.stime is null and pj.etime is null) and pj.enable = true ")
    fun findByDS(t: BigDecimal, jobtypeid: Long?): List<Pijob>?

    @Query("from Pijob pj where (pj.tlow <= ?1 and pj.thigh >=?1) and pj.job_id = ?2" + " and (pj.stime is null and pj.etime is null and pj.enable = ?3) ")
    fun findByDS(t: BigDecimal, jobtypeid: Long?, e: Boolean?): List<Pijob>?

    @Query("from Pijob pj order by pj.tlow  ")
    fun fintAllOrderByT(page: Pageable): List<*>?

    @Query("from Pijob pj where (pj.tlow <= ?1 and pj.thigh >=?1)  and pj.job_id = ?3 and (pj.stime >= ?2 and pj.etime <= ?2) and pj.enable=true")
    fun findByDS(t: BigDecimal, time: Date, jobid: Long?): List<*>?

    @Query("from Pijob pj where pj.job_id =?1 and pj.enable = true")
    fun findByJob_id(id: Long?): List<Pijob>?

    @Query("from Pijob pj where pj.pidevice_id = ?1 order by pj.id ")
    fun searchByDeviceid(id: Long?, page: Pageable): List<Pijob>?

    /**
     * สำหรับค้นหา one command
     *
     * @param b
     * @return
     */
    @Query("from Pijob pj where pj.job_id = null and pj.enable = true")
    fun findByOne(b: Boolean): List<Pijob>?

    @Query("from Pijob p where p.pidevice.name like %?1% or p.name like %?1%  order by p.pidevice_id,p.id")
    override fun search(search: String, topage: Pageable): List<Pijob>?

    @Query("from Pijob p where (p.pidevice.name like %?1% or p.name like %?1%) and p.addby = ?2  order by p.pidevice_id,p.id")
    fun search(search: String, uid: Long, topage: Pageable): List<Pijob>?

    @Query("from Pijob p where p.job_id = ?1 and p.ds18sensor_id =?2 and ( p.tlow <= ?3 and p.thigh >= ?3) and p.enable = true")
    fun DSBysensor(dsjobid: Long, sensorid: Long, t: BigDecimal): ArrayList<Pijob>?

    @Query("from Pijob p where p.job_id =?1  and p.enable = true")
    fun findDSOther(jobid: Long): ArrayList<Pijob>?

    fun findByNameAndAddby(n: String, uid: Long): Pijob?
    @Query("from Pijob p where p.enable = true and p.job_id = ?2 and (p.lowtime <= ?1 and p.hightime >=?1 )")
    fun fineByTime(currenttime: Long, jobid: Long): List<Pijob>?

    @Modifying
    @Transactional
    @Query("delete from Pijob p where p.id = ?1")
    fun deletePijobById(id: Long)

    fun findByJob_idAndEnable(jobid: Long, b: Boolean): List<Pijob>?

}
