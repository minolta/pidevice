package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import org.hibernate.annotations.Cache
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity

/**
 * งานต่างๆที่มีในระบบ
 *
 * @author kykub
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Cacheable
@Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
class Job(var refid: Long? = null, var name: String? = null,
          @Column(columnDefinition = "text") var description: String? = null,var checkversion:Long?=0) : En() {

    override fun toString(): String {
        return "Job [id " + this.id + " refid=" + refid + ", name=" + name + ", description=" + description + "]"
    }

}
