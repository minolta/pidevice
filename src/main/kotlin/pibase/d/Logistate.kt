package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import org.hibernate.annotations.Cache
import javax.persistence.Cacheable
import javax.persistence.Entity

/*
 * สำหรับเก็บค่า hi low
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Logistate : En() {

    var name: String? = null
    var refid: Long? = null

    override fun toString(): String {
        return "Logistate [name=$name, refid=$refid]"
    }

}
