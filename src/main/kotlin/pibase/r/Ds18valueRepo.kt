package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import me.pixka.kt.pibase.d.DS18value
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface Ds18valueRepo : JpaRepository<DS18value, Long>
//        search<DS18value>
        {

    fun findTop1ByOrderByIdDesc(): DS18value?

    fun findTop100ByToserver(b: Boolean): List<DS18value>?
    fun findTop500ByToserver(b: Boolean): List<DS18value>?

    @Query(" from  DS18value  t where t.pidevice_id = ?1 and t.valuedate >= ?2 and t.valuedate <= ?3 order by t.valuedate ")
    fun findgraphvalue(device_id: Long?, s: Date, e: Date): List<*>?

    fun findTop1ByOrderByValuedateDesc(): DS18value?

    fun findTop1ByIdOrderByValuedateDesc(id: Long?): DS18value?

    fun findTop1ByPidevice_idOrderByValuedateDesc(id: Long?): DS18value?

    fun findTop1ByDs18sensor_idOrderByValuedateDesc(id: Long?): DS18value?

    @Query(" from  DS18value  t where t.ds18sensor_id = ?1 and t.valuedate >= ?2 and t.valuedate <= ?3 order by t.valuedate ")
    fun findgraphvalueBySensor(sid: Long?, s: Date, e: Date): List<DS18value>?

//    @Query("from DS18value d where  ")
//    override fun search(search: String, topage: Pageable): List<DS18value>?

    @Modifying
    @Transactional
    @Query("delete from DS18value d where d.toserver = true")
    fun cleanToserver()
}
