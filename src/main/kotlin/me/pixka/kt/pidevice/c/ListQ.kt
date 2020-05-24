package me.pixka.kt.pidevice.c

import me.pixka.kt.pidevice.t.QueueService
import org.springframework.web.bind.annotation.*
import java.util.ArrayList


@RestController
class ListQ (val service:QueueService)
{
    @CrossOrigin
    @RequestMapping(value = ["/lq"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun getthread(): List<Any>? {

        var q = service.queue
        return q
    }
    @CrossOrigin
    @RequestMapping(value = ["/rq"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun resetq(): Int {

        service.queue.clear()
        return service.queue.size
    }

}