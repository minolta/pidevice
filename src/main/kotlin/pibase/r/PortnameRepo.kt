package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Portname

@Repository
interface PortnameRepo : JpaRepository<Portname, Long>, search<Portname>, findByName<Portname> {


    @Query("from Portname pn where pn.name like %?1% or pn.piport like %?1%")
    override fun search(s: String, page: Pageable): List<Portname>?

    fun findByRefid(id: Long?): Portname?

}
