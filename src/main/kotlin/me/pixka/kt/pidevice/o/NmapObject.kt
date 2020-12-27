package me.pixka.kt.pidevice.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
class NmapObject (var ip:String?=null,var mac:String?=null)

