package me.pixka.kt.pidevice.c

import me.pixka.kt.pidevice.s.ReportLowPerssureObject
import me.pixka.kt.pidevice.s.WarterLowPressureService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class WaterLowperssureControl(val lps:WarterLowPressureService) {

    @GetMapping(path=["/listlows"])
    fun listlows(): ArrayList<ReportLowPerssureObject> {
        return lps.reports
    }
    @GetMapping(path=["/setlowmax/{max}"])
    fun setlowmax(@PathVariable("max")max:Int): Int {
        lps.maxcount=max
        return max
    }
    @GetMapping(path=["/waterlowstatus"])
    fun warterlowstatus(): Boolean {
        return lps.canuse
    }
    @GetMapping(path=["/togerhjob"])
    fun settoger()
    {
        lps.canuse=!lps.canuse
    }
    @GetMapping(path=["/resetlows"])
    fun resetLow(): Boolean {
        return lps.reset()
    }
}