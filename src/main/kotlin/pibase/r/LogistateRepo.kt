package me.pixka.pibase.r

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Logistate
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query

@Repository
interface LogistateRepo : JpaRepository<Logistate, Long> {

    fun findByName(n: String): Logistate?

    fun findByRefid(id: Long?): Logistate?
    @Query("from Logistate l where l.name like %?1%")
    fun search(search: String, topage: Pageable): List<Logistate>?

}
