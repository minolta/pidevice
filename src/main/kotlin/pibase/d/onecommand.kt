package me.pixka.kt.pibase.d

import me.pixka.kt.base.d.En
import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.transaction.Transactional

@Entity
class Onecommand(var name: String? = null, @ManyToOne var pidevice: PiDevice? = null,
                 @Column(insertable = false, updatable = false) var pidevice_id: Long? = null,
                 var run: Boolean? = false, @ManyToOne var pijob: Pijob? = null,
                 @Column(insertable = false, updatable = false) var pijob_id: Long? = null) : En() {
}


@Repository
interface OnecommandRepo : JpaRepository<Onecommand, Long>, search<Onecommand>, findByName<Onecommand> {

    fun findByPidevice_idAndRun(id: Long, run: Boolean): List<Onecommand>?
    @Query("from Onecommand o where o.name like %?1%")
    override fun search(search: String, topage: Pageable): List<Onecommand>?

    @Modifying
    @Transactional
    @Query("delete from Onecommand p where p.pijob_id = ?1")
    fun deleteBypijob(id: Long)
}