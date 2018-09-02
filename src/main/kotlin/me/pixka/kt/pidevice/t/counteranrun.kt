package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.run.Worker
import me.pixka.pibase.s.PortstatusinjobService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled

@Profile("pi", "lite")
class CounterandRun(var pj: Pijob, var gi: GpioService, val m: MessageService, val i: Piio, val ppp: PortstatusinjobService)
    : Worker(pj, gi, i, ppp) {




}