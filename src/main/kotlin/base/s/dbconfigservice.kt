package me.pixka.kt.base.s

import me.pixka.kt.base.d.Dbconfig
import me.pixka.kt.base.d.DbconfigRepo
import org.springframework.stereotype.Service

@Service
class DbconfigService(val r: DbconfigRepo) : DefaultService<Dbconfig>() {



    fun findorcreate(name: String, value: String): Dbconfig {
        var o = findByName(name)
        if (o == null) {
            o = Dbconfig()
            o.name = name
            o.value = value
            o = save(o)
        }

        return o!!
    }

}