package me.pixka.kt.pibase.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.pibase.d.Logistate
@JsonIgnoreProperties(ignoreUnknown = true)
class Logicobj(val obj: Logistate?=null) {

}