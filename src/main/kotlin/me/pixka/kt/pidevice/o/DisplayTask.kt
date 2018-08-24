package me.pixka.kt.pidevice.o

import me.pixka.kt.run.DSDPWorker
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.stereotype.Component
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Component
class DisplayTask(var d: DSDPWorker? = null) {


    @Async("aa")
    fun run(): Future<Boolean>? {

        if (d != null) {
            var result = d?.rs()
            return AsyncResult(result)

        }

        return AsyncResult(false)
    }

}