package me.pixka.kt.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Mixdata
import me.pixka.kt.pibase.d.MixdataRepo
import org.springframework.stereotype.Service
import java.util.*

@Service
class Mixdataservice( val r:MixdataRepo) : DefaultService<Mixdata>() {
    fun findByDate(s: Date, e:Date):List<Mixdata>?
    {
        return r.findByDate(s,e)
    }
}