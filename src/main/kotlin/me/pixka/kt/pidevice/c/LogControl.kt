package me.pixka.kt.pidevice.c

import me.pixka.base.o.SearchOption
import me.pixka.log.d.LogService
import me.pixka.log.d.Logsevent
import org.springframework.web.bind.annotation.*


@RestController
class LogControl (val service:LogService)
{
    @CrossOrigin
    @RequestMapping(value = ["/listlog"], method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun listSC(@RequestBody searchOption: SearchOption): List<Logsevent>? {

        var s = searchOption.s?.time
        var e = searchOption.e?.time
        var n = searchOption.search
        return service.findByDateAndName(s!!,e!!,n!!)
    }
}