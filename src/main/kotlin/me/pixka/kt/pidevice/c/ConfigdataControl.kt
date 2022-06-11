package me.pixka.kt.pidevice.c

import me.pixka.kt.pidevice.d.Configdata
import me.pixka.kt.pidevice.d.ConfigdataService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigdataControl(val cs: ConfigdataService) {

    @GetMapping("/setconfig/{name}/{value}")
    fun setConfig(@PathVariable("name") name: String, @PathVariable("value") value: String): Configdata {

        var c = cs.findOrCreate(name, value)
        c.valuename = value
        return cs.save(c)

    }

    @GetMapping("/allconfig")
    fun allconfig(): MutableList<Configdata> {
        return cs.all()
    }
}