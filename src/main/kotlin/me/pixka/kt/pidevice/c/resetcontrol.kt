package me.pixka.kt.pidevice.c

import me.pixka.pibase.s.*
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File

@RestController
@Profile("pi", "lite")
class ResetControl(val portstatusinjobService: PortstatusinjobService,val pijobService: PijobService,val ps:PideviceService,val ss:DS18sensorService)
{


    @CrossOrigin
    @RequestMapping(value = "/resetpijob", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): Boolean? {

        portstatusinjobService.clear()
        pijobService.clear()
        ps.clear()
        ss.clear()
        return true
    }

}