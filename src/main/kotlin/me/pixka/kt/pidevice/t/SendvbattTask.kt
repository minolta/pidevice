package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.t.HttpPostTask
import me.pixka.kt.pidevice.d.VbattService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class Sendvbatt(val service: VbattService) {

    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    fun sendtask() {

        println("Send v batt " + Date())
        var target = System.getProperty("piserver") + "/vbatt/add"
        var list = service.nottoserver()
        var t = Executors.newSingleThreadExecutor()
        if (list != null && !list.isEmpty()) {

            list.map {


                var task = HttpPostTask(target, it)
                var f = t.submit(task)

                try {
                    var re = f.get(5, TimeUnit.SECONDS)
                    if (re.statusLine.statusCode == 200) {
                        it.toserver = true
                        var v = service.save(it)
                        logger.debug("Saved vbatt:" + v)
                    } else {
                        logger.error("ERROR ${re.statusLine.statusCode} ")
                    }
                } catch (e: Exception) {
                    logger.error("Send ds18vale error ${e.message}")
                }
            }
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Sendvbatt::class.java)
    }
}