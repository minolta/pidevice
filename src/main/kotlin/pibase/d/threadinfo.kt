package me.pixka.kt.pibase.d

import me.pixka.kt.base.d.En
import me.pixka.kt.base.r.REPOBase
import me.pixka.kt.base.s.ServiceImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne


@Entity
class Threadinfo(@ManyToOne var pidevice: PiDevice? = null, @Column(insertable = false, updatable = false)
var pidevice_id: Long? = null,
                 @Column(columnDefinition = "text") var info: String? = null) : En() {

}


@Repository
interface ThreadinfoRepo : JpaRepository<Threadinfo, Long>, REPOBase<Threadinfo> {
    fun findByPidevice_id(id: Long): Threadinfo?

    @Query("from Threadinfo d where d.pidevice.name like %?1%")
    override fun search(search: String?, page: Pageable): List<Threadinfo>?
}


@Service
class ThreadinfoService(val r: ThreadinfoRepo) : ServiceImpl<Threadinfo>() {
    fun findBydeviceId(id: Long): Threadinfo? {
        return r.findByPidevice_id(id)
    }

    override fun search(search: String, page: Long, limit: Long): List<Threadinfo>? {
        return r.search(search, topage(page, limit))
    }



}
