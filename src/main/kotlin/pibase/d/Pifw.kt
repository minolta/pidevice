package me.pixka.kt.pibase.d

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
class Pifw : En() {

    @ManyToOne
    var pifwgroup: Pifwgroup? = null
    @Column(insertable = false, updatable = false)
    var pifwgroup_id: Long? = null
    var verno: String? = null
    var checksum: String? = null
    var pathtofile: String? = null
    var filename: String? = null
    var fwsize: Long? = null

}
