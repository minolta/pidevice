package me.pixka.pibase.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import me.pixka.kt.pibase.d.Pijob

@JsonIgnoreProperties(ignoreUnknown = true)
class Pijobobj(var id: Long? = null,
               var enable: Boolean? = true,
               var pijob: Pijob? = null


               ,var ports: List<Portstatusobj>? = null

)




