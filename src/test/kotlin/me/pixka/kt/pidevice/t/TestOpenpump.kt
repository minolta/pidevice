package me.pixka.kt.pidevice.t

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TestService
import me.pixka.kt.pidevice.worker.OpenpumpsWorker
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@DataJpaTest
class TestOpenpump {

    @Autowired
    lateinit var testService: TestService
    var status = ""
    @Test
    fun testOpenpumps()
    {

        var mtp = mockk<MactoipService>(relaxed = true)

        var data = testService.portstatusinjob()

        data[0].runtime =200
        data[0].device?.ip = "192.168.88.21"

        data[1].runtime = 1000
        data[1].device?.ip= "192.168.88.3"


        every { mtp.http.get("http://192.168.88.21/run?delay=200") } returns "ok"
        every { mtp.http.get("http://192.168.88.3/run?delay=1000") } returns "null"

        data.forEach {
            CompletableFuture.supplyAsync {
                var re = ""
                var timetoopen = 10
                try {
                    timetoopen  = it.runtime!!
                    var url = "http://${it.device?.ip}/run?delay=${timetoopen}"
                    re = mtp.http.get(url, 2000)
                }catch (e:Exception)
                {
                    logger.error("Open pumps Error ${it.device?.name} ${timetoopen}")
                    throw e
                }
                " Run ok OPEN PUMP : ${it.device?.name} ${re} RUN ${timetoopen}"
            }.thenAccept {
                status = ""
            }.exceptionally {

                logger.error("Error Job ${it.message}")
                it.printStackTrace()
                null

            }
        }

        TimeUnit.SECONDS.sleep(1)

        verify { mtp.http.get("http://192.168.88.21/run?delay=200") }
        verify { mtp.http.get("http://192.168.88.3/run?delay=1000") }
    }

    var logger = LoggerFactory.getLogger(TestOpenpump::class.java)

}