package me.pixka.pibase.o

import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Dhtvalue

class Infoobj {

    var mac: String? = null
    var ip: String? = null
    var dhtvalue: Dhtvalue? = null
    var ds18value: DS18value? = null
    var token:String?=null
    var password: String? = null

    override fun toString(): String
    {
        return "${mac} ${ip} ${dhtvalue} ${ds18value}"
    }
}
