package me.pixka.kt.pidevice.t

import me.pixka.log.d.LogService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("!test")
class DeleteLog(val ls:LogService)
{
    @Scheduled(fixedDelay = 60000)
    @Transactional
    fun clearLog()
    {
        ls.delete(true)
    }
}