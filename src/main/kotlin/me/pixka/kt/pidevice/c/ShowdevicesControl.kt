package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.s.PideviceService
import org.springframework.web.bind.annotation.*

@RestController
class ShowdevicesControl (val ps:PideviceService){

    @CrossOrigin
    @RequestMapping(value = ["/devices"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun getthread(): List<Any>? {

        return ps.all()
    }
}