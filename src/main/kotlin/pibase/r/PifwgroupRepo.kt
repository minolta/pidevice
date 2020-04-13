package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Pifwgroup
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query

@Repository
interface PifwgroupRepo : JpaRepository<Pifwgroup, Long>, search<Pifwgroup>, findByName<Pifwgroup> {

    @Query("from Pifwgroup p where p.name like %?1%")
    override fun search(search: String, topage: Pageable): List<Pifwgroup>?

}
