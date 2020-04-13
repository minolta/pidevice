package me.pixka.kt.pibase.d

import me.pixka.kt.base.d.En
import org.hibernate.annotations.Cache
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*
import javax.persistence.Cacheable
import javax.persistence.Entity

@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Mixdata(var name: String? = null,
              var mixdate: Date? = null, var mixcount: Long? = null,
              var checksum: BigDecimal? = null,var totalsawdust:BigDecimal?=null,
              var totalwater:BigDecimal?=null,var totalpot:Long?=0/*สำหรับบอกว่ายอดก้อนเท่าไหร่*/) : En() {
    constructor() : this(name = "")

}

@Repository
interface MixdataRepo : JpaRepository<Mixdata, Long> {

    @Query("from Mixdata m where m.name like %?1%")
    fun search(search: String, topage: Pageable): List<Mixdata>?

    @Query("from Mixdata m where m.mixdate >= ?1 and m.mixdate <= ?2")
    fun findByDate(s: Date, e: Date): List<Mixdata>?
}
