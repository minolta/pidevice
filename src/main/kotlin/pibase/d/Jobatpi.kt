package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import me.pixka.kt.pibase.d.Job
import me.pixka.kt.pibase.d.PiDevice
import org.hibernate.annotations.Cache
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

/**
 * สำหรับเก็บ job ไว้ run ใน pi device
 *
 * @author kykub
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Jobatpi : En() {

    @ManyToOne
    var pidevice: PiDevice? = null
    @Column(insertable = false, updatable = false)
    var pidevice_id: Long? = null

    @ManyToOne
    var job: Job? = null
    @Column(insertable = false, updatable = false)
    var job_id: Long? = null

}
