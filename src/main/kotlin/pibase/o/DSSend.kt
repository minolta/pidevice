package me.pixka.pibase.o

import java.math.BigDecimal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class DSSend {
    var t: BigDecimal? = null

    var ip: String? = null
    var adddate: String? = null
}