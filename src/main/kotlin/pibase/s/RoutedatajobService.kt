package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.pibase.d.Routedatajob
import me.pixka.kt.pibase.r.RoutedatajobRepo
import org.springframework.stereotype.Service

@Service
class RoutedatajobService(val r: RoutedatajobRepo) : DefaultService<Routedatajob>() {
    fun searchMatch(n: String): Routedatajob? {
        return r.findByName(n)
    }

    fun create(name: String, description: String, url: String): Routedatajob {
        val i = Routedatajob()
        i.name = name
        i.description = description
        i.url = url
        return save(i)!!
    }
}
