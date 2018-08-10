package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import java.util.*

interface PijobrunInterface {
    fun setP(pijob: Pijob)
    fun setG(gpios: GpioService)
    fun runStatus(): Boolean
    fun getPijobid(): Long
    fun getPJ(): Pijob
    fun startRun(): Date?
    fun state(): String? //สำหรับบอกว่าทำอะไรอยู่
}