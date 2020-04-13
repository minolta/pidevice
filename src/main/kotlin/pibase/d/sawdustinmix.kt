package me.pixka.kt.pibase.d

import me.pixka.kt.base.d.En
import org.hibernate.annotations.Cache
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne


@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Sawdustinmix(@ManyToOne var mixdata: Mixdata? = null,var orderno:Int?=null,
                   @Column(insertable = false, updatable = false) var mixdata_id: Long? = null,
                   var value: BigDecimal? = null) : En()


@Repository
interface SawdustinmixRepo : JpaRepository<Sawdustinmix, Long> {
    fun findByMixdata_id(id: Long): List<Sawdustinmix>?
    //@Query("from Sawdustinmix ")
    //fun search(search: String, topage: Pageable): List<Sawdustinmix>?
}


