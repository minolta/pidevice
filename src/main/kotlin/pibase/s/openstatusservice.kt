package me.pixka.kt.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.OpenStatus
import me.pixka.kt.pibase.d.OpenstatusRepo
import org.springframework.stereotype.Service

@Service
class OpenstatusService( val r: OpenstatusRepo) : DefaultService<OpenStatus>() {


    fun findOpen(): OpenStatus? {
        return r.findTop1ByOpenOrderByAdddateDesc(true)
    }
}