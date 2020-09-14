package me.pixka.kt.pidevice.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.t.HttpGetTask
import me.pixka.kt.run.PijobrunInterface
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


class Dustworker(var pijob: Pijob, var ports: ArrayList<Portstatusinjob>) :
        PijobrunInterface, Runnable, Callable<Pm> {
    var om = ObjectMapper()
    lateinit var startRun: Date
    lateinit var state: String
    override fun setP(pijob: Pijob) {
        TODO("Not yet implemented")
    }

    override fun setG(gpios: GpioService) {
        TODO("Not yet implemented")
    }

    override fun runStatus(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun getPJ(): Pijob {
        TODO("Not yet implemented")
    }

    override fun startRun(): Date? {
        return startRun
    }

    override fun state(): String? {
        TODO("Not yet implemented")
    }

    override fun setrun(p: Boolean) {
        TODO("Not yet implemented")
    }

    override fun run() {



        ports.forEach {

            var http = HttpGetTask("http://" + it.device?.ip!!)

            var f = Executors.newSingleThreadExecutor().submit(http)

            var re = f.get()

            var pm = om.readValue<Pm>(re!!)

            println(pm)

        }

    }

    fun runport() {

        ports.forEach {
            println(it)
        }
    }

    override fun call(): Pm {
        var ee = Executors.newSingleThreadExecutor()
        val completableFuture = CompletableFuture<Pm>()

        try {
            var http = HttpGetTask("http://" + pijob.desdevice?.ip!!)
            var f = ee.submit(http)
            var re = f.get()
            var pm = om.readValue<Pm>(re!!)

            var pm25 = pm.pm25?.toFloat()
            if (pm25!! >= pijob.tlow?.toFloat()!! && pm25 <= pijob.thigh?.toFloat()!!) {
                runport()
            }
            return pm
        } catch (e: Exception) {
            throw e
        }

    }

}