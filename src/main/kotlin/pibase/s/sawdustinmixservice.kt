package me.pixka.kt.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Sawdustinmix
import me.pixka.kt.pibase.d.SawdustinmixRepo
import org.springframework.stereotype.Service

@Service
class SawdustinmixService( val r: SawdustinmixRepo) : DefaultService<Sawdustinmix>() {

    fun findByMixdataId(id:Long):List<Sawdustinmix>?
    {
        return r.findByMixdata_id(id)
    }
}