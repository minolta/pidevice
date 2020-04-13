package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.t.HttpPostTask
import me.pixka.kt.pidevice.d.CountfgService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class SendCountfg(val service: CountfgService) {
    val om = ObjectMapper()
    var ex = Executors.newSingleThreadExecutor()
    @Scheduled(fixedDelay = 6000)
    fun send() {
        var sendto = System.getProperty("sendcountfg")


        var forsend = service.nottoserver()

        logger.debug("Send traget ${sendto} forsend ${forsend}")
        if (forsend != null)
            for (t in forsend) {
                var http = HttpPostTask("${sendto}", t)
                try {
                    var f = ex.submit(http)
                    var re = f.get(10, TimeUnit.SECONDS)
                    if (re.statusLine.statusCode == 200) {
                        t.toserver = true
                        service.save(t)
                        logger.debug("Send ok")
                    }
                    else
                    {
                        logger.error("Error ${re.statusLine.statusCode}")
                    }

                } catch (e: Exception) {
                    logger.error("Send ERROR ${e.message}")
                }
            }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(SendCountfg::class.java)
    }

}