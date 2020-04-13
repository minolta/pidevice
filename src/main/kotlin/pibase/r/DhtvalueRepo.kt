package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import java.util.Date

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Dhtvalue
import org.springframework.data.jpa.repository.Modifying
import javax.transaction.Transactional

@Repository
interface DhtvalueRepo : JpaRepository<Dhtvalue, Long> {

    fun findTop1ByOrderByIdDesc(): Dhtvalue?

    fun findByToserver(b: Boolean, page: Pageable): List<*>?

    @Query("select hour(t.valuedate),day(t.valuedate),month(t.valuedate),year(t.valuedate),avg(t.h),avg(t.t) from Dhtvalue  t where t.pidevice_id = ?1 and t.valuedate >= ?2 and valuedate <= ?3 group by hour(t.valuedate),day(t.valuedate),month(t.valuedate),year(t.valuedate) order by year(t.valuedate),month(t.valuedate),day(t.valuedate),hour(t.valuedate) ")
    fun findgraph(device_id: Long?, s: Date, e: Date): List<*>?

    fun findTop1ByOrderByValuedateDesc(): Dhtvalue?
    @Modifying
    @Transactional
    @Query("delete from Dhtvalue d where d.toserver = true")
    fun deleteBySend()

    fun findTop1ByPidevice_idOrderByValuedateDesc(id: Long): Dhtvalue?

}
