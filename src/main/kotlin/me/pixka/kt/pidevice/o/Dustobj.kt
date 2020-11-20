package me.pixka.kt.pidevice.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
class Dustobj (var pm25:BigDecimal?=null,var pm10:BigDecimal?=null,var pm1:BigDecimal?=null)