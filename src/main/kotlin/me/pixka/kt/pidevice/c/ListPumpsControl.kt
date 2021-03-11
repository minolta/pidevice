package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import org.springframework.web.bind.annotation.*

@RestController
class ListPumpsControl(val pus:PumpforpijobService) {


    @CrossOrigin
    @RequestMapping(value = ["/pumps"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun pumps(): MutableList<Pumpforpijob> {
        return pus.all()
    }

}