package me.pixka.kt.pidevice.c

import org.springframework.web.bind.annotation.*

@RestController
class TestParamter {

    @CrossOrigin
    @RequestMapping(value = ["/tp/{name}"], method = arrayOf(RequestMethod.GET))
    fun testGetName(@PathVariable("name") name: String): String {
        println(name)
        return name
    }
}
