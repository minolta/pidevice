package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Pifwgroup
import me.pixka.pibase.r.PifwgroupRepo
import org.springframework.stereotype.Service

@Service
class PifwgroupService( val r: PifwgroupRepo) : DefaultService<Pifwgroup>() {


    fun searchMatch(n: String): Pifwgroup? {
        return null
    }

    fun findorcreate(appname: String): Pifwgroup? {
        var pn: Pifwgroup? = r.findByName(appname)
        if (pn == null) {
            pn = Pifwgroup()
            pn.name = appname
            pn = save(pn)
        }
        return pn
    }

}
