package me.pixka.pibase.o

import java.math.BigDecimal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Forgraph {

    var t: BigDecimal? = null
    var h: BigDecimal? = null
    var hour: Long? = null
    var day: Long? = null
    var month: Long? = null
    var year: Long? = null
}
