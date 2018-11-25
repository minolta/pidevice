package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pidevice.s.TaskService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TaskList(val taskService: TaskService)
{


    @CrossOrigin
    @RequestMapping(value = "/listtask", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun list(): ArrayList<tl> {
        var list = ArrayList<tl>()

        var runs = taskService.runinglist
        for(run in runs)
        {
            var pj = run.getPJ()
            var t = tl(run.getPijobid(),run.getPJ().name,run.startRun(),run.state(),run.runStatus(),pj.ports)
            list.add(t)
        }

        return list
    }
}

class tl(var id:Long?=null,var name:String?=null, var startrun: Date?=null,
         var state:String?=null,var runstatus:Boolean?=null,var ports:List<Portstatusinjob>?=null)