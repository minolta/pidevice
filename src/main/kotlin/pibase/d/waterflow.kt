package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import me.pixka.kt.base.r.REPOBase
import me.pixka.kt.base.s.ServiceImpl
import org.hibernate.annotations.Cache
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.persistence.Cacheable
import javax.persistence.Entity
import java.util.*
import javax.persistence.ManyToOne

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Waterflow(var name: String? = null, var waterflowtime: Date? = null,
                @ManyToOne var pidevice: PiDevice? = null, var waterflowvalue: BigDecimal? = null, var rawvalue: BigDecimal? = null) : En()

@Repository
interface WaterflowRepo : JpaRepository<Waterflow, Long>, REPOBase<Waterflow> {
    @Query("from Waterflow w where w.pidevice.name like %?1% ")
    override fun search(search: String?, page: Pageable): List<Waterflow>?
}


@Service
class WaterflowService(val r: WaterflowRepo) : ServiceImpl<Waterflow>() {


}