package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.t.ThreadInfo
import me.pixka.kt.run.PijobrunInterface
import me.pixka.pi.io.Dotmatrix
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@RestController
@Profile("pi", "lite")
class NameControl(val dps: DisplayService, val io: Piio, val tsk: TaskService, val context: ApplicationContext) {

    val version = "1.0.6"
    @CrossOrigin
    @RequestMapping(value = "/name", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): String? {
        val bufferedReader: BufferedReader = File("/home/pi/name").bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        return inputString
    }

    @CrossOrigin
    @RequestMapping(value = "/showname", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun showname() {


        logger.debug("Run Display status")

        try {

            if (!dps.lock) {
                var dot = lockdisplay()
                if (dot != null) {
                    var a = io.wifiIpAddress()
                    var ip = io.wifiMacAddress()
                    dot.showMessage("Name : ${a}  IP: ${ip}  version : ${version}")
                    unlock()
                    TimeUnit.MILLISECONDS.sleep(500)
                    dot = lockdisplay()
                }
                //  TimeUnit.SECONDS.sleep(1)
            } else {
                logger.error("Time out")
            }
        } catch (e: Exception) {
            logger.error("Error: ${e.message}")
        }

    }

    fun lockdisplay(): Dotmatrix? {
        var count = 0
        while (dps.lock) {
            TimeUnit.MILLISECONDS.sleep(200)
            count++
            if (count > 100) {
                logger.error("Error Display timeout")
                return null
            }
        }
        return dps.lockdisplay(this)
    }

    fun unlock() {
        dps.unlock(this)
    }


    @CrossOrigin
    @RequestMapping(value = "/version", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun version(): String? {
        return version
    }


    @CrossOrigin
    @RequestMapping(value = "/thread", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun showthread(): ShowThread {
        var b = HashMap<String, String>()
        var buf = ArrayList<Runobj>()
        var t = context.getBean("taskScheduler") as ThreadPoolTaskScheduler
        var tp = context.getBean("pool") as ExecutorService
        var aa = context.getBean("aa") as ThreadPoolExecutor
        val queue = t.scheduledThreadPoolExecutor
        val s = queue.queue

        ThreadInfo.logger.info("Scheduler active count: ${t.activeCount} pool size: ${t.poolSize}  Queue size:${s.size} ")
        b.put("activecount", "${t.activeCount}")
        b.put("poolsize", "${t.poolSize}")
        b.put("queuesize", "${s.size}")

        var tt = tp as ThreadPoolExecutor

        ThreadInfo.logger.info("Pool TaskService run: ${tt.activeCount}  Queue size: ${tt.queue.size} Pool size: ${tt.poolSize} Pool max size : ${tt.corePoolSize} / ${tt.maximumPoolSize} Complete ${tt.completedTaskCount}")
        b.put("ttpooltask", "${tt.activeCount}")
        b.put("ttqueuesize", "${tt.queue.size}")
        b.put("ttpoolsize", "${tt.poolSize}")
        if (tt.activeCount > 0) {

            for (run in tsk.runinglist) {
                ThreadInfo.logger.debug("Runs id : " + run.getPijobid().toString() + " " + run.runStatus())

                var info = "Runs id : " + run.getPijobid().toString() + " " + run.runStatus()
                var r = Runobj(info, run.getPJ(), run.startRun(),run.state())
                buf.add(r)
            }
        }

        ThreadInfo.logger.info("Asyn AT: ${aa.activeCount} pool size: ${aa.poolSize}  Queue size:${aa.queue}  c:${aa.completedTaskCount}")
        b.put("aapooltask", "${aa.activeCount}")
        b.put("aaqueuesize", "${aa.queue.size}")
        b.put("aapoolsize", "${aa.poolSize}")
        b.put("aapoolsize", "${aa.completedTaskCount}")

        var re = ShowThread()
        re.info = b
        re.runs = buf
        return re
        /*
         var o = s.iterator()
         for(run in o)
         {
             logger.info("run : ${run}")
         }*/
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(NameControl::class.java)
    }
}

class Runobj(var info: String? = null, var pijob: Pijob? = null, var startRun: Date? = null,var state:String?=null)

class ShowThread(var info: HashMap<String, String>? = null, var runs: ArrayList<Runobj>? = null)