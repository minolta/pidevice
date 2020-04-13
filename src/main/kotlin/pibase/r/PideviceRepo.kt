package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import me.pixka.kt.pibase.d.PiDevice
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface PideviceRepo : JpaRepository<PiDevice, Long>, search<PiDevice>, findByName<PiDevice> {

    fun findByMac(s: String): PiDevice?

    @Query("from PiDevice p where p.name like %?1% or p.mac like %?1% order by p.id")
    override fun search(s: String, page: Pageable): List<PiDevice>?

    @Query("from PiDevice p where (p.name like %?1% or p.mac like %?1%) and p.user_id = ?2 order by p.id")
    fun search(s: String, uid: Long, page: Pageable): List<PiDevice>?

    fun findByRefid(id: Long): PiDevice?

    @Modifying
    @Transactional
    @Query("delete from PiDevice ")
    fun clear()

    @Query(value = "show tables", nativeQuery = true)
    fun showtables(): List<*>?

}
