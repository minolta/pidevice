package me.pixka.kt.pibase.d

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.base.d.En
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
class Usewaterinformation(@ManyToOne var pidevice: PiDevice? = null,
                          @Column(insertable = false, updatable = false) var pidevice_id: Long?=null,
                          var enduse: Date? = null, var devicegroupid: Long? = null,
                          var pijob_id: Long? = null, var end: Boolean? = false,var status:Int?=0,
                          var message:String?=null) : En() {

    override fun toString(): String {
        return "${pidevice} ${enduse} ${end} ${status}"
    }
}

//สำหรับใช้ส่งการใช้น้ำไปยังศูนย์ข้อมูลน้ำ
class Waterinfo(var mac: String? = null, var enduse: Date? = null,var pijob:Pijob?=null,var groupid:Long?=null)
{
    override fun toString(): String {
        return "MAC:${mac} ${enduse}"
    }
}


@Repository
interface UsewaterinformationRepo : JpaRepository<Usewaterinformation, Long> {
    fun findTop1ByEnduse(b: Boolean): Usewaterinformation?
    fun findTop1ByEnd(b: Boolean): Usewaterinformation?
    fun findTop1ByEndAndDevicegroupid(b:Boolean,id:Long):Usewaterinformation?
}
