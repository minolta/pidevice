package me.pixka.kt.pidevice.c

import org.springframework.web.bind.annotation.*

@RestController
class VerControl()
{
    @CrossOrigin
    @RequestMapping(value = ["/ver"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun ver(): String {
        return "1.0"
    }

}