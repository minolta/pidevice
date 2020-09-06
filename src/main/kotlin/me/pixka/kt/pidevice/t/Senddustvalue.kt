package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.t.HttpPostTask
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class Senddustvalue(val server: PmService) {


    @Scheduled(fixedDelay = 5000)
    fun run() {
        var host = System.getProperty("piserver")
        var datas = server.findByToServer(false)

        if (datas != null) {
            datas.forEach {

                try {
                    var h = HttpPostTask(host + "/pm/add", it)
                    var f = Executors.newSingleThreadExecutor().submit(h)
                    var r = f.get(2,TimeUnit.SECONDS)
                    it.toserver = true
                    server.save(it)
                } catch (e: Exception) {

                }

            }
        }
    }

}