package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Job
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.t.HttpGetTask
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


@Component
//@Profile("pi", "lite")
class LoadMainJobTask(val jobService: JobService) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 15000)
    fun run() {
        var traget = System.getProperty("piserver")

        var url = "${traget}/job/lists/0/1000"
        var t = Executors.newSingleThreadExecutor()
        var http = HttpGetTask(url)
        var f = t.submit(http)

        try {
            var r = f.get(5, TimeUnit.SECONDS)
            println(r)
            val list = om.readValue<List<Job>>(r!!)
            for (j in list) {
                var job = jobService.findByName(j.name!!)
                if (job == null) {
                    var nj = Job()
                    nj.name = j.name
                    nj.refid = j.id
                    jobService.save(nj)
                    logger.info("New Job in localhost ${nj}")
                }
            }

        } catch (e: Exception) {
            logger.error("Load main job ${e.message} ")
        }


    }


    companion object {
        internal var logger = LoggerFactory.getLogger(LoadMainJobTask::class.java)
    }
}

@Component
@Profile("pi", "lite")
class LoadjobTask(val service: JobService,
                  val http: HttpControl) {


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
            throw var4
        }

    }

    fun setup() {

        logger.debug("[loadmainjob] setup()")
        System.getProperty("piserver")

        var host = System.getProperty("piserver")
        if (host == null)
            host = System.getProperty("hosttarget", "http://pi1.pixka.me")

        target = host + "/job/lists/0/1000"
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LoadjobTask::class.java)
    }

}