package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pidevice.s.TaskService
import org.springframework.web.bind.annotation.*

@RestController
class KillControl (val task:TaskService)
{
    @CrossOrigin
    @RequestMapping(value = ["/kill/{jobid}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun kill(@PathVariable("jobid")id:Long): Boolean {

        var job = task.runinglist.find { it.getPijobid() == id }
        if(job!=null && job.runStatus()) {
            job.setrun(false)
            return true
        }
        return false
    }
}