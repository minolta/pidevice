package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.d.Portname
import me.pixka.pibase.s.PortnameService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Component
@Profile("pi", "lite")
class LoadPortnameTask(val task: LoadPortTask) {


    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    fun run() {
        logger.info("starttask Start loadportname")

        var f = task.run()
        var count = 0
        while (true) {
            if (f!!.isDone) {
                logger.info("LoadPort job done")
                break
            }

            TimeUnit.SECONDS.sleep(1)
            count++
            if (count > 30) {
                logger.error("Time out")
                f.cancel(true)
            }
        }

        logger.info("End load Port")
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(LoadPortnameTask::class.java)
    }
}

@Component
class LoadPortTask(val service: PortnameService,
                   val cfg: DbconfigService,
                   val http: HttpControl,
                   val err: ErrorlogService) {
    private var target = "http://192.168.69.50:5002/portname/lists/0/1000"
    private val om = ObjectMapper()
    @Async("aa")
    fun run(): Future<Boolean>? {
        setup()

        try {
            val list = loadfromservice()
            if (list != null)
                savetodevice(list)
            return AsyncResult(true)
        } catch (e: Exception) {
            logger.error("Error Portname : " + e.message)
            err.n("Load port name", "35", e.message!!)
        }

        return null
    }

    fun savetodevice(list: List<Portname>) {

        try {
            for (item in list) {
                var j: Portname? = service.findByRefid(item.id)
                if (j == null) {

                    j = service.create(item)
                    j = service.save(j)
                    logger.debug("[savetodevice Portname] Save :  " + j!!)
                }
            }
        } catch (e: Exception) {
            logger.error("savetodevice Portname error : " + e.message)
            err.n("Load port name", "53", e.message!!)
        }

    }

    private fun loadfromservice(): List<Portname>? {

        try {
            val re = http.get(target)
            val list = om.readValue<List<Portname>>(re)
            return list
        } catch (e: IOException) {
            logger.error("loadfromservice Error Portname: " + e.message)
            err.n("Load port name", "66", e.message!!)
        }

        return null
    }

    private fun setup() {
        logger.debug("[Portname] setup")

        var host = System.getProperty("piserver")
        if (host == null)
            host = cfg.findorcreate("hosttarget", "http://pi1.pixka.me").value
        target = host + "/portname/lists/0/1000"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LoadPortTask::class.java)
    }
}
