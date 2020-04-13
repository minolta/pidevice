package me.pixka.kt.base.s

import me.pixka.kt.base.d.Errorlog
import me.pixka.kt.base.d.ErrorlogRepo
import org.springframework.stereotype.Service
import java.util.*

@Service
class ErrorlogService(val r: ErrorlogRepo) : Ds<Errorlog>() {
    override fun search(search: String, page: Long, limit: Long): List<Errorlog>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun n(local: String, line: String, message: String) {
        var e = Errorlog()
        e.line = line
        e.location = local
        e.message = message
        e.adddate = Date()
        e = save(e)!!
    }

    fun searchbyDate(search: String, s: Date?, e: Date?, page: Long = 0, limit: Long = 50): List<Errorlog>? = r.searchbyDate(search, s, e, topage(page, limit))

}