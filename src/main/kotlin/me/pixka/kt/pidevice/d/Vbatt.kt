package me.pixka.kt.pidevice.d

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pidevice.s.DefaultService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne


@Entity
class Vbatt(@ManyToOne var pidevice: PiDevice? = null,
            @Column(insertable = false, updatable = false) var pidevice_id: Long? = null,
            var v: BigDecimal? = null, var valuedate: Date? = null,var toserver:Boolean?=false) : BaseIdEntity()

@Repository
interface VbattRepo : JpaRepository<Vbatt, Long>
{
    fun findByToserver(b:Boolean):List<Vbatt>?
}

@Service
class VbattService (val r:VbattRepo): DefaultService<Vbatt>()
{
    fun nottoserver(): List<Vbatt>? {
        return r.findByToserver(false)
    }
}