package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class Loadpijobs(val http: HttpControl) {

    var target = ""
    var targetloadstatus = ""

    fun setup() {

        var host = System.getProperty("piserver")
        target = host + "/pijob/lists"
        targetloadstatus = host + "/portstatusinjob/lists"
    }

    fun loadPijob(mac: String): List<Pijob>? {

        try {
            val mapper = ObjectMapper()
            val re = http.get(target + "/" + mac)
            val list = mapper.readValue<List<Pijob>>(re)
            logger.debug("[pijob loadpijob] Found Jobs for me " + list.size)
            return list
        } catch (e: IOException) {
            logger.error("[loadpijob] :error:" + e.message)
            return null
        }
    }

    fun loadPortstate(pijobid: Long?): List<Portstatusinjob>? {
        try {
            val mapper = ObjectMapper()
            val re = http.get(targetloadstatus + "/" + pijobid)
            val list = mapper.readValue<List<Portstatusinjob>>(re)
            LoadpijobTask.logger.debug("[loadpijob] Found Port states  for me " + list.size)
            return list
        } catch (e: IOException) {
            logger.error("[loadpijob] Load port status : " + e.message)
        }

        return null
    }
    companion object {
        internal var logger = LoggerFactory.getLogger(Loadpijobs::class.java)
    }

}