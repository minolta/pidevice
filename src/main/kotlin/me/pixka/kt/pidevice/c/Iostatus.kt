package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.s.GpioService
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController
@Profile("pi", "lite")
class IostatusControl(val gpios:GpioService)
{

    @CrossOrigin
    @RequestMapping(value = ["/io"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): String? {

        return "IO 22 ${gpios.in22value} IO 23 ${gpios.in23value}"
    }
}