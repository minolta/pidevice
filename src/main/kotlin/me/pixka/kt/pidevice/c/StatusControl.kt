package me.pixka.kt.pidevice.c

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.pixka.kt.pidevice.t.UptimeTask
import org.springframework.web.bind.annotation.*

@RestController
class StatusControl (val uptime:UptimeTask)
{
    @CrossOrigin
    @RequestMapping(value = ["/"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun value(): String {

        return "{\"uptime\": ${uptime.uptime}}"
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class Statusobj(var uptime:Long?=0,var status:String?=null,var psi:Double?=null)