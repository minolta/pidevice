package me.pixka.kt.pidevice.c

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TaskList(val taskService: TaskService, val pjs: PijobService, val readUtil: ReadUtil,
               val ips:IptableServicekt) {


    @CrossOrigin
    @RequestMapping(value = "/listtask", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun list(): ArrayList<tl> {
        var list = ArrayList<tl>()

        var runs = taskService.runinglist
        for (run in runs) {
            var pj = run.getPJ()
            var t = tl(run.getPijobid(), run.getPJ().name, run.startRun(), run.state(), run.runStatus(), pj.ports)
            list.add(t)
        }

        return list
    }

    @CrossOrigin
    @RequestMapping(value = "/listjobs", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun listjob(): MutableList<Pijob>? {

        return pjs.all()
    }

    @CrossOrigin
    @RequestMapping(value = "/testjob/{localid}", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun testjob(@PathVariable("localid") id: Long): Boolean {

        try {
            var job = pjs.findByRefid(id)
            logger.debug("Job ${job}")

            var url = readUtil.findurl(job.desdevice_id!!, job.ds18sensor_id)
            logger.debug("Url ${url} ${job.desdevice_id} ${job.ds18sensor_id} ")
            return readUtil.checktmp(job)
        } catch (e: Exception) {
            logger.error("${e.message}")
            throw e
        }
    }


    @CrossOrigin
    @RequestMapping(value = "/listips", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun listips(): MutableList<Iptableskt>? {

        return  ips.all()
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TaskList::class.java)
    }
}

class tl(var id: Long? = null, var name: String? = null, var startrun: Date? = null,
         var state: String? = null, var runstatus: Boolean? = null, var ports: List<Portstatusinjob>? = null)