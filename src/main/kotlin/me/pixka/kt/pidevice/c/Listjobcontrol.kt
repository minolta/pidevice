package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.PijobrunInterface
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

@RestController
class TaskList(val taskService: TaskService, val pjs: PijobService, val readUtil: ReadUtil,
               val ips: IptableServicekt, val context: ApplicationContext) {


    @CrossOrigin
    @RequestMapping(value = ["/tx"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun getthread(): List<Any>? {

        try {
            var buf = ArrayList<tl>()
            var list = taskService.runinglist
            for (i in list) {
                var t = tl()
                t.id = i.getPijobid()
                t.name = i.getPJ().name
                t.startrun = i.startRun()
                t.state = i.state()
                buf.add(t)
            }
            return buf

//            return taskService.runinglist as List<Any>
        } catch (e: Exception) {
            logger.error("TXERROR ${e.message}")
            return null
        }
    }


    @CrossOrigin
    @RequestMapping(value = ["/threadinfo"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun threadinfo(): ThreadinfoCount {

        var p = taskService.pool as ThreadPoolExecutor

        var t = ThreadinfoCount()
        t.activecount = p.activeCount
        t.coresize = p.corePoolSize
        t.queuesize = p.queue.size

        return t


    }


    fun short(b: ArrayList<tl>) {
        var bb = ArrayList<tl>()
        val array = arrayOfNulls<tl>(b.size)
        var a = arrayOfNulls<tl>(b.size)
        b.toArray(array)
        var t: tl? = null
        var v: tl? = null
        for (i in 0..b.size) {
            if (t == null) {
                t = array[i]
                continue
            }

            for (j in i..b.size) {
                if (array[j]?.id!! < t.id!!) {
                    v = array[j]

                }
            }

        }

    }

    @CrossOrigin
    @RequestMapping(value = ["/listtask"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun list(): ArrayList<tl> {

        var list = ArrayList<tl>()
        var runs = taskService.runinglist
        for (run in runs) {
            try {
                var pj = run.getPJ()
                var t = tl(run.getPijobid(), run.getPJ().name, run.startRun(), run.state(),
                        run.runStatus(), pj.ports, run.getPJ().job?.name, run.exitdate(), run.getPJ().refid)
                list.add(t)
            } catch (e: Exception) {
                logger.error("List task error ${e.message}")
                throw e
            }
        }

        return list

    }

    @CrossOrigin
    @RequestMapping(value = ["/active"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun threadactive(): Int {
        var runs = taskService.pool as ThreadPoolExecutor
        return runs.activeCount

    }

    @CrossOrigin
    @RequestMapping(value = ["/core"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun threadcore(): Int {
        var runs = taskService.pool as ThreadPoolExecutor
        return runs.corePoolSize
    }

    @CrossOrigin
    @RequestMapping(value = ["/queuesize"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun queuesize(): Int {
        var runs = taskService.pool as ThreadPoolExecutor
        return runs.queue.size
    }

    @CrossOrigin
    @RequestMapping(value = ["/runs"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun runs(): Int {

        return taskService.runinglist.size
    }

    @CrossOrigin
    @RequestMapping(value = ["/runsactive"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun runslist(): List<tl> {
        var re = taskService.runinglist.filter { it.runStatus() }.map {

            var tl = getTl(it)
            tl
        }

        return re
    }

    fun getTl(it: PijobrunInterface): tl {
        var tl = tl(it.getPijobid(), it.getPJ().name, it.startRun(), it.state(),
                it.runStatus(), it.getPJ().ports, it.getPJ().job?.name, it.exitdate(), it.getPJ().refid)
        tl.name = it.getPJ().name
        return tl
    }

    @CrossOrigin
    @RequestMapping(value = ["/l3/{name}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun lt(@PathVariable("name") name: String): ArrayList<tl> {
        logger.debug("Call Tl")
        var n = name.toLowerCase()
        var list = ArrayList<tl>()
        var runs = taskService.runinglist.filter { it.getPJ().name?.toLowerCase()?.indexOf(n) != -1 }

        for (run in runs) {
            try {
                var t = getTl(run)
                list.add(t)
            } catch (e: Exception) {
                logger.error("List task error ${e.message}")
                throw e
            }
        }

        return list

    }

    @CrossOrigin
    @RequestMapping(value = ["/liststask"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun listSC() {

    }

    @CrossOrigin
    @RequestMapping(value = ["/listpool"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun listpool(): ArrayList<tl> {
        var tp = context.getBean("pool") as ExecutorService
        var list = ArrayList<tl>()
        val threadSet = Thread.getAllStackTraces().keys
        logger.debug("threadpool =======================Strart list ================================")
        var i = 1
        for (thread in threadSet) {
            if (thread is PijobrunInterface) {
                var t = tl(thread.getPijobid(), thread.getPJ().name, thread.startRun(), thread.state(), thread.runStatus(), null)
                list.add(t)
                i++
            } else
//                if (thread is Worker) {
//                    var t = tl(thread.getPijobid(), thread.getPJ().name, thread.startRun(), thread.state(), thread.runStatus(), null)
////                logger.debug("threadpool ${i}: ===> ID:${thread.id} NAME:${thread.name} RUN:${thread.isAlive} FULL:${thread}")
//                    list.add(t)
//                    i++
//                } else if (thread is DefaultWorker) {
//                    var t = tl(thread.getPijobid(), thread.getPJ().name, thread.startRun(), thread.state(), thread.runStatus(), null)
////                logger.debug("threadpool ${i}: ===> ID:${thread.id} NAME:${thread.name} RUN:${thread.isAlive} FULL:${thread}")
//                    list.add(t)
//                    i++
//                }
                try {
                    var t = thread as PijobrunInterface
                    var tt = tl(t.getPijobid(), t.getPJ().name, t.startRun(), t.state(), t.runStatus(), null)
//                logger.debug("threadpool ${i}: ===> ID:${thread.id} NAME:${thread.name} RUN:${thread.isAlive} FULL:${thread}")
                    list.add(tt)
                    i++
                } catch (e: Exception) {
                    logger.error("Can not case ****** ")
                }
            logger.debug("threadpoollist ${i}: ===> ID:${thread.id} NAME:${thread.name} RUN:${thread.isAlive} state:${thread.state}")

        }
        logger.debug("threadpool ======================= end ================================")
        return list
    }

    @CrossOrigin
    @RequestMapping(value = ["/listjobs"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun listjob(): MutableList<Pijob>? {

        return pjs.all()
    }

    @CrossOrigin
    @RequestMapping(value = ["/testjob/{localid}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun testjob(@PathVariable("localid") id: Long): Boolean {

        try {
            var job = pjs.findByRefid(id)
            logger.debug("Job ${job}")

            var url = readUtil.findurl(job?.desdevice_id!!, job.ds18sensor_id)
            logger.debug("Url ${url} ${job.desdevice_id} ${job.ds18sensor_id} ")
            return readUtil.checktmp(job)
        } catch (e: Exception) {
            logger.error("${e.message}")
            throw e
        }
    }


    @CrossOrigin
    @RequestMapping(value = ["/listips"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun listips(): MutableList<Iptableskt>? {

        return ips.all()
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskList::class.java)
    }
}

class tl(var id: Long? = null, var name: String? = null, var startrun: Date? = null,
         var state: String? = null, var runstatus: Boolean? = null, var ports: List<Portstatusinjob>? = null,
         var jobtype: String? = null, var exitdate: Date? = null, var refid: Long? = 0)


class ThreadinfoCount(var activecount: Int? = null, var coresize: Int? = null, var queuesize: Int? = null)

