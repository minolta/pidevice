package me.pixka.pibase.r

import me.pixka.kt.pibase.d.Pifw
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PifwRepo : JpaRepository<Pifw, Long> {

    fun findByVerno(ver: String): Pifw?

    @Query("from Pifw p where p.verno = ?1 and p.pifwgroup.name = ?2")
    fun findByVernoAndApp(ver: String, app: String): Pifw?


    fun findTop1ByOrderByIdDesc(): Pifw?


    fun findTop1ByPifwgroup_idOrderByIdDesc(groupid: Long?): Pifw?
    @Query("from Pifw p where p.verno like %?1%")
    fun search(search: String, topage: Pageable): List<Pifw>?



}
