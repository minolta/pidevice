package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.DS18sensor
import org.springframework.data.jpa.repository.Modifying
import javax.transaction.Transactional

@Repository
interface Ds18sensorRepo : JpaRepository<DS18sensor, Long>, search<DS18sensor>, findByName<DS18sensor> {
    @Query("from DS18sensor d where d.name like %?1% or d.callname like %?1% order by d.id")
    override fun search(s: String, page: Pageable): List<DS18sensor>?


//    @Query("from DS18sensor d where d.name like %?1% or d.callname like %?1% order by d.id")
//    fun search(s: String, page: Pageable?): List<DS18sensor>?


    @Query("from DS18sensor d where (d.name like %?1% or d.callname like %?1%) and d.user_id = ?2 order by d.id")
    fun search(s: String, uid: Long, page: Pageable?): List<DS18sensor>?

    fun findTop1ByForreadOrderByLasteditDesc(b: Boolean): DS18sensor?
    @Modifying
    @Transactional
    @Query("delete from DS18sensor ")
    fun clear()
}
