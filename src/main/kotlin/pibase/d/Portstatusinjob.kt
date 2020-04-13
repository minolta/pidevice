package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import org.hibernate.annotations.Cache
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Portstatusinjob(@ManyToOne var portname: Portname? = null,
                      @Column(insertable = false, updatable = false) var portname_id: Long? = null,
                      var refid: Long? = null,
                      @ManyToOne var status: Logistate? = null,
                      @Column(insertable = false, updatable = false) var status_id: Long? = null,
                      @JsonBackReference
                      @ManyToOne
                      var pijob: Pijob? = null,
                      @Column(insertable = false, updatable = false)
                      var pijob_id: Long? = null,
                      var enable: Boolean? = true,
                      var checkversion: Long? = 0,
                      var runtime: Int? = 0,
                      var waittime: Int? = 0,
                      @ManyToOne var device: PiDevice?=null, //บอกว่าไปเปิด port ที่ตัวไหน
                      @Column(insertable = false, updatable = false) var device_id: Long? = null

) : En() {

    fun copy(from: Portstatusinjob) {
        if(verref==null || verref!=from.ver) {
            this.portname = from.portname
            this.status = from.status
            verref = from.ver
        }
    }

    override fun toString(): String {
        return "${portname?.name} = ${status} ${enable} ${device} ${runtime} ${waittime} "
    }
}
