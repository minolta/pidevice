package me.pixka.pibase.r

import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Job
import org.springframework.data.jpa.repository.Modifying
import javax.transaction.Transactional

@Repository
interface JobRepo : JpaRepository<Job, Long>, search<Job>, findByName<Job> {


    fun findByRefid(id: Long?): Job?

    fun findTop1ByName(name: String): Job?

    @Query("from Job j where j.name like %?1%")
    override fun search(s: String, page: Pageable): List<Job>?

    @Modifying
    @Transactional
    @Query("delete from Job ")
    fun clear()
}
