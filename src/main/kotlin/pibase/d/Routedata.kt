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
class Routedata : En() {

    @ManyToOne
    var ds18sensor: DS18sensor? = null
    @Column(insertable = false, updatable = false)
    var de18sensor_id: Long? = null
    var name: String? = null
    var srcmac: String? = null // ต้นทาง
    var desmac: String? = null // ปลายทาง
    var todo: String? = null// ทำอะไร ใช่ อ่านค่า ก็ /dhtvalue /ds18value
    // /display/value หรืออื่นๆ
    var enable: Boolean? = true
    var port = "80"

    var refid: Long? = null // ใช้สำหรับเก็บ id ที่อยู่ที่ Server

    @ManyToOne
    var job: Job? = null // งานที่ต้องทำ

    @Column(insertable = false, updatable = false)
    var job_id: Long? = null

    @Column(columnDefinition = "text")
    var description: String? = null

    @ManyToOne
    var routedatajob: Routedatajob? = null

    @Column(insertable = false, updatable = false)
    var routedatajob_id: Long? = null

    override fun toString(): String {
        return ("Routedata [name=" + name + ", srcmac=" + srcmac + ", desmac=" + desmac + ", todo=" + todo + ", enable="
                + enable + ", port=" + port + ", refid=" + refid + ", job=" + job + ", job_id=" + job_id
                + ", description=" + description + ", routedatajob=" + routedatajob + ", routedatajob_id="
                + routedatajob_id + "]")
    }

    fun copy(rd: Routedata) {

        this.description = rd.description
        this.desmac = rd.desmac
        this.srcmac = rd.srcmac
        this.enable = rd.enable
        this.name = rd.name
        this.job = rd.job
    }

}
