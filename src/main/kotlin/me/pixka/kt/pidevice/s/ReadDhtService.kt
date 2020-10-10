package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.DHTObject
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReadDhtService(val http: HttpService,val lgs:LogService) {
    val om = ObjectMapper()

    fun iptodhtobj(ip: String): DHTObject {
        try {
            var re = http.get("http://${ip}", 5000)
            var dhtobj = om.readValue<DHTObject>(re)
            return dhtobj
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),"ReadDhtService",
            "","16","iptodhtobj")
            throw e
        }

    }



}