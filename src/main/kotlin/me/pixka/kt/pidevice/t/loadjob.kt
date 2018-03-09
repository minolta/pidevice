package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.pibase.d.Job
import me.pixka.pibase.s.JobService
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
@Profile("pi","lite")
class LoadMainJobTask(val task: LoadjobTask) {


    @Scheduled(initialDelay = 30000, fixedDelay = 60000)
    fun run() {
        logger.info("in loadmainjob")

        var count = 0
        try {
            logger.debug("in while  loadmainjob")

            var f = task.run()
            while (true) {
                if (f!!.isDone) {
                    break
                    logger.info("End load job ")
                }
                TimeUnit.SECONDS.sleep(1)
                count++
                if (count > 30) {
                    logger.error("Time out")
                    f.cancel(true)
                }
            }

        } catch (e: Exception) {
            logger.error("Error loadmainjob : " + e.message)

        }

    }


    companion object {
        internal var logger = LoggerFactory.getLogger(LoadMainJobTask::class.java)
    }
}

@Component
@Profile("pi","lite")
class LoadjobTask(val service: JobService,
                  val dbcfg: DbconfigService,
                  val http: HttpControl, val err: ErrorlogService) {


    // load /pijob/list/{mac}
    private var target = "http://192.168.69.2:5002/job/lists/0/1000"

    @Async("aa")
    fun run(): Future<Boolean>? {
        try {
            setup()
            val list = loadjobfromServer(target)
            for (j in list) {
                var job = service.findByName(j.name!!)
                if (job == null) {
                    var nj = Job()
                    nj.name = j.name
                    nj.refid = j.id
                    service.save(nj)
                }
            }
            return AsyncResult(true)
        } catch (e: Exception) {
            logger.error(e.message)
        }

        return null
    }

    fun loadjobfromServer(target: String): List<Job> {
        try {
            val om = ObjectMapper()
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

    fun setup() {

        logger.debug("[loadmainjob] setup()")
        System.getProperty("piserver")

        var host = System.getProperty("piserver")
        if (host == null)
            host = dbcfg.findorcreate("hosttarget", "http://pi1.pixka.me").value

        target = host + "/job/lists/0/1000"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LoadjobTask::class.java)
    }

}