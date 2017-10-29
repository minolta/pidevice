package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.DisplayService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class Displayruning(val dps: DisplayService) {


    @Scheduled(fixedDelay = 1000)
    fun run() {
        if (!dps.lock) {
            var dot = dps.lockdisplay(any = this)
            dot.letter(2, '.'.toShort())
            TimeUnit.SECONDS.sleep(1)
            dot.letter(2, ' '.toShort())
            dps.unlock(this)
            //  TimeUnit.SECONDS.sleep(1)
        }
    }


}