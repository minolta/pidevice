package me.pixka.kt.pibase.r

import me.pixka.kt.base.s.search
import me.pixka.kt.pibase.d.Routedatajob
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoutedatajobRepo : JpaRepository<Routedatajob, Long>, search<Routedatajob> {

    @Query("from Routedatajob r where r.name like %?1% or r.url like %?1%")
    override fun search(s: String, page: Pageable): List<Routedatajob>?

    fun findByName(n: String): Routedatajob?

}
