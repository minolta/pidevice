package me.pixka.kt.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Devicegroup
import me.pixka.kt.pibase.d.DevicegroupRepo
import org.springframework.stereotype.Service

@Service
class DevicegroupService( val r: DevicegroupRepo) : DefaultService<Devicegroup>() {


}