package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import me.pixka.kt.base.s.findByName
import me.pixka.kt.base.s.search
import org.hibernate.annotations.Cache
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

/**
 * สำหรับบอกว่า แต่ละ device ทำการเปิด น้ำบ้างจะได้ไม่เปิดซ้ำกัน
 */
/*

@ManyToOne var piDevice: PiDevice? = null,
                 @Column(insertable = false, updatable = false) var piDevice_id: Long? = null,
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class OpenStatus(var opendate: Date? = null, var closedate: Date? = null, @ManyToOne var pidevice: PiDevice? = null,
                 @Column(insertable = false, updatable = false) var pidevice_id: Long? = null,
                 var open: Boolean, var gid: Long? = null) : En() {
    constructor() : this(open = true)
}


@Repository
interface OpenstatusRepo : JpaRepository<OpenStatus, Long>, search<OpenStatus> {
    @Query("from OpenStatus o where o.pidevice.name like %?1%")
   override fun search(search: String, topage: Pageable): List<OpenStatus>?

    fun findTop1ByOpenOrderByAdddateDesc(open: Boolean): OpenStatus?
}
