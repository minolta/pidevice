package me.pixka.kt.pidevice.t

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UptimeTask()
{
    var uptime:Long = 0

    @Scheduled(fixedDelay = 1000)
    fun up()
    {
        uptime++
    }
}