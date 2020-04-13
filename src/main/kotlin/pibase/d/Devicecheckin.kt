package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import me.pixka.kt.pibase.d.PiDevice
import org.hibernate.annotations.Cache
import java.util.*
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Devicecheckin(@ManyToOne var pidevice: PiDevice? = null,
                    @Column(insertable = false, updatable = false) var pidevice_id: Long? = null,
                    var ip: String? = null, var checkindate: Date? = null,
                    var password: String? = null) : En() {

}
