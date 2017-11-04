package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.pibase.d.Job
import me.pixka.pibase.s.JobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException


@Component
@Profile("pi")
class LoadMainJobTask(val service: JobService,
                      val dbcfg: DbconfigService,
                      val http: HttpControl, val err: ErrorlogService) {

    // load /pijob/list/{mac}
    private var target = "http://192.168.69.2:5002/job/lists/0/1000"

    var om = jacksonObjectMapper()
    @Scheduled(initialDelay = 30000,fixedDelay = 60000)
    fun run() {
        logger.info("in loadmainjob")
        setup()
        try {
            logger.debug("in while  loadmainjob")
            val list = loadjobfromServer(target)
            for(j in list)
            {
                var job = service.findByName(j.name!!)
                if(job == null)
                {
                    var nj = Job()
                    nj.name = j.name
                    nj.refid = j.id
                    service.save(nj)
                }
            }


        } catch (e: Exception) {
            logger.error("Error loadmainjob : " + e.message)
            err.n("Loadjob", "35", e.message!!)
        }

    }

    @Throws(IOException::class)
    fun loadjobfromServer(target: String): List<Job> {
        try {
            val re = http.get(target)
            logger.debug("loadmainjob target : $target result: $re")
            val list = om.readValue<List<Job>>(re)
            return list
        } catch (var4: IOException) {
            logger.error("loadfromservice Error loadmainjob: " + var4.message)
            err.n("Loadjob", "48", var4.message!!)
            throw var4
        }

    }

    private fun setup() {
        logger.debug("[loadmainjob] setup()")
        var host = dbcfg.findorcreate("hosttarget","http://pi1.pixka.me").value
        target  = host+dbcfg.findorcreate("serviceloadmainjob",":5002/job/lists/0/1000").value!!
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LoadMainJobTask::class.java)
    }
}