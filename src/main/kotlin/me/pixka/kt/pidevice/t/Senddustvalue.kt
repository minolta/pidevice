package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.run.Status
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Senddustvalue(val server: PmService, val httpService: HttpService) {

    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {
        var host = System.getProperty("piserver")
        var datas = server.findByToServer(false)

        if (datas != null) {
            datas.forEach {

                try {
                    var h = httpService.post(host + "/pm/add", it)
                    var r = om.readValue<Status>(h)
                    it.toserver = true
                    server.save(it)
                } catch (e: Exception) {

                }

            }
        }
    }

}