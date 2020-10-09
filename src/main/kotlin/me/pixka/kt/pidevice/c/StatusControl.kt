package me.pixka.kt.pidevice.c

import me.pixka.kt.pidevice.t.UptimeTask
import org.springframework.web.bind.annotation.*

@RestController
class StatusControl (val uptime:UptimeTask)
{
    @CrossOrigin
    @RequestMapping(value = ["/"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun value(): Statusobj {

        return Statusobj(uptime.uptime)
    }
}

class Statusobj(var uptime:Long?=0)