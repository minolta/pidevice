package me.pixka.kt.pidevice.t

import me.pixka.log.d.LogService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class DeleteLog(val ls:LogService)
{
    @Scheduled(fixedDelay = 60000)
    @Transactional
    fun clearLog()
    {
        ls.delete(true)
    }
}