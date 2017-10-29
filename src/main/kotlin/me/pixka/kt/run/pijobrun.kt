package me.pixka.kt.run

import me.pixka.kt.pibase.s.GpioService
import me.pixka.pibase.d.Pijob

interface PijobrunInterface {
    fun setP(pijob: Pijob)
    fun setG(gpios:GpioService)
    fun runStatus():Boolean
    fun getPijobid():Long
}