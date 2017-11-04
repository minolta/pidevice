package me.pixka.kt.pidevice.t

import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.Ds18valueService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class CleanDsvalue(val ds:Ds18valueService) {

    @Scheduled(cron = "0 0 4 * * *")
    fun clean()
    {
        ds.cleanToserver()
    }
}