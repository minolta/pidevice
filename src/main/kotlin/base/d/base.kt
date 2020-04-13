package me.pixka.kt.base.d

import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.*

@MappedSuperclass
open class En(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long,
              @CreatedDate var adddate: Date? = null,
              @Version var ver: Int? = 0,var addby:Long?=null,
              var lastedit:Date?=null,var verref:Int?=0) {
    constructor() : this(id = 0) {
    }
}