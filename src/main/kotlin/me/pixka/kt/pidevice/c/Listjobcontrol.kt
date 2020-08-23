package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.DefaultWorker
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.Worker
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.ExecutorService

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
                        run.runStatus(), pj.ports,run.getPJ().job?.name)
                list.add(t)
            } catch (e: Exception) {
                logger.error("List task error ${e.message}")
                throw e
            }
        }

        return list

    }

    @CrossOrigin
    @RequestMapping(value = ["/l3/{name}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun lt(@PathVariable("name") name: String): ArrayList<tl> {
        logger.debug("Call Tl")
        var list = ArrayList<tl>()
        var runs = taskService.runinglist
        var n = name.toLowerCase()
        for (run in runs) {
            try {
                var pj = run.getPJ()
                if (run.getPJ() != null && run.getPJ().name?.toLowerCase()?.indexOf(n) != -1) {
                    var t = tl(run.getPijobid(), run.getPJ().name, run.startRun(), run.state()
                            , run.runStatus(), pj.ports,run.getPJ().job?.name)
                    list.add(t)
                }
            } catch (e: Exception) {
                logger.error("List task error ${e.message}")
                throw e
            }
        }

        return list

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
                if (thread is Worker) {
                    var t = tl(thread.getPijobid(), thread.getPJ().name, thread.startRun(), thread.state(), thread.runStatus(), null)
//                logger.debug("threadpool ${i}: ===> ID:${thread.id} NAME:${thread.name} RUN:${thread.isAlive} FULL:${thread}")
                    list.add(t)
                    i++
                } else if (thread is DefaultWorker) {
                    var t = tl(thread.getPijobid(), thread.getPJ().name, thread.startRun(), thread.state(), thread.runStatus(), null)
//                logger.debug("threadpool ${i}: ===> ID:${thread.id} NAME:${thread.name} RUN:${thread.isAlive} FULL:${thread}")
                    list.add(t)
                    i++
                }
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
         var jobtype:String?=null)