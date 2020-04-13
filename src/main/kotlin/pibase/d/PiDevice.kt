package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import me.pixka.kt.pibase.d.Devicegroup
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
class PiDevice(
        var name: String? = null,
        var mac: String? = null,// last ip
        var code: String? = null,
        var ip: String? = null, var password: String? = null,
        @Column(columnDefinition = "text") var description: String? = null,
        var lastupdate: Date? = null,
        var refid:Long?=null,
        @ManyToOne var devicegroup: Devicegroup? = null,
        @Column(insertable = false, updatable = false) var devicegroup_id: Long? = null,
        var user_id: Long? = null) : En() {

    constructor() : this(ip = null)


    override fun toString(): String {
        return "Device : ${name} ${mac}"
    }
}
