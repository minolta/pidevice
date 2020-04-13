package me.pixka.kt.base.c

import me.pixka.kt.base.d.Dbconfig
import me.pixka.kt.base.o.SearchOption
import me.pixka.kt.base.s.DbconfigService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*


@RestController
class Dbconfigcontrol(val service: DbconfigService) {
    companion object {
        internal var logger = LoggerFactory.getLogger(Dbconfigcontrol::class.java)
    }

    @RequestMapping(value = ["/rest/dbconfig/sn"], method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"), consumes = arrayOf("application/json"))
    @ResponseBody
    @CrossOrigin
    fun search(@RequestBody search: SearchOption): List<Dbconfig>? {

        logger.debug("Search ${search}")
        return service.search(search.search!!, search.page!!, search.limit!!)
    }


    @RequestMapping(value = ["/rest/dbconfig/get/{id}"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @CrossOrigin
    fun get(@PathVariable("id") id: Long): Dbconfig? {

        return service.find(id)
    }


    @RequestMapping(value = ["/rest/dbconfig/sm"], method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"), consumes = arrayOf("application/json"))
    @ResponseBody
    @CrossOrigin
    fun searchm(@RequestBody search: SearchOption): Dbconfig? {

        logger.debug("Search ${search}")
        return service.findByName(search.search!!)
    }

    @RequestMapping(value = ["/rest/dbconfig/add"], method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"), consumes = arrayOf("application/json"))
    @ResponseBody
    @CrossOrigin
    fun add(@RequestBody db: Dbconfig): Dbconfig? {

        return service.save(db)

    }

    @RequestMapping(value = ["/rest/dbconfig/edit"], method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"), consumes = arrayOf("application/json"))
    @ResponseBody
    @CrossOrigin
    fun edit(@RequestBody db: Dbconfig): Dbconfig? {

        return service.save(db)

    }
}