package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.PijobService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GetpijobsControl(val pjs:PijobService) {

    @GetMapping("/pijobs")
    fun getpijob(): MutableList<Pijob> {
        return pjs.all()
    }
}