package me.pixka.pibase.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.pibase.o.Logicobj

@JsonIgnoreProperties(ignoreUnknown = true)
class Portstatusobj(var id: Long? = null, var portname: Portobj? = null,
                    var status: String? = null,
                    var logic: Logicobj? = null,
                    var enable: Boolean? = null
) {


}
