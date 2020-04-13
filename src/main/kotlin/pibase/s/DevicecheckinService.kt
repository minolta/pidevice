package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Devicecheckin
import me.pixka.pibase.r.DevicecheckinRepo
import org.springframework.stereotype.Service

@Service
class DevicecheckinService( val r: DevicecheckinRepo) : DefaultService<Devicecheckin>() {
    fun last(id: Long): Devicecheckin? {
        return r.findTop1ByPidevice_idOrderByIdDesc(id)
    }
}
