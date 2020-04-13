package me.pixka.kt.pidevice.d

import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.*


@MappedSuperclass
open class BaseIdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreatedDate
    var adddate: Date? = null

    @Version
    var ver = 0
    var addby: Long? = null
    var lastedit: Date? = null

}