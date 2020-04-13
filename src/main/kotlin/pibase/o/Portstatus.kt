package me.pixka.pibase.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import me.pixka.kt.pibase.d.Portname

@JsonIgnoreProperties(ignoreUnknown = true)
class Portstatus {

    var portname: Portname? = null
    var status: String? = null
}
