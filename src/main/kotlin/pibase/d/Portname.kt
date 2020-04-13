package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import org.hibernate.annotations.Cache
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

/**
 * Port ของ rasberry pi
 *
 * @author kykub
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Portname(var name: String? = null, var piport: String? = null, var refid: Long? = null,var portenable:Boolean?=false,
               @ManyToOne var portmode: Portmode? = null,
               @Column(insertable = false, updatable = false) var portmode_id: Long? = null,
               var checkversion:Long?=0) : En() {

}
