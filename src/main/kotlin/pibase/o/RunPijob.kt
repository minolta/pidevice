package me.pixka.pibase.o

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.pi4j.io.gpio.GpioController

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob

@JsonIgnoreProperties(ignoreUnknown = true)
class RunPijob : Runnable {

    var isIsrun = true

    var pijob: Pijob? = null
    var ports: List<Portstatusinjob>? = null
    private var gpio: GpioController? = null

    override fun run() {

        println("[runpijob] Run " + this.pijob!!)
        setport(ports)
        this.isIsrun = false
    }

    private fun setport(list: List<Portstatusinjob>?) {

        for (psj in list!!) {
            println("[runpijob port]" + psj)
        }
    }

    fun setGpio(gpio: GpioController) {
        this.gpio = gpio
    }

}
