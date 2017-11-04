package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.pibase.d.Portname
import me.pixka.pibase.s.PortnameService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.TimeUnit

@Component
@Profile("pi")
class LoadPortnameTask(val service: PortnameService, val cfg: DbconfigService, val http: HttpControl,val err:ErrorlogService) {

    private var target = "http://192.168.69.50:5002/portname/lists/0/1000"

    private val om = jacksonObjectMapper()

    @Scheduled(initialDelay = 60*1000*5,fixedDelay = 60000)
    fun run() {
        logger.info("starttask Start loadportname")
        setup()

            try {
                val list = loadfromservice()
                savetodevice(list!!)

            } catch (e: Exception) {
                logger.error("Error Portname : " + e.message)
                err.n("Load port name","35",e.message!!)
            }
    }

    private fun savetodevice(list: List<Portname>) {

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
            err.n("Load port name","53",e.message!!)
        }

    }

    private fun loadfromservice(): List<Portname>? {

        try {
            val re = http!!.get(target)
            val list = om.readValue<List<Portname>>(re)
            return list
        } catch (e: IOException) {
            logger.error("loadfromservice Error Portname: " + e.message)
            err.n("Load port name","66",e.message!!)
        }

        return null
    }

    private fun setup() {
        logger.debug("[Portname] setup")
        var host = cfg.findorcreate("hosttarget","http://pi1.pixka.me").value
        target = host+cfg.findorcreate("serviceloadportname",":5002/portname/lists/0/1000").value!!
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LoadPortnameTask::class.java)
    }
}
