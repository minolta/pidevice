package me.pixka.kt.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Mixedinfo
import me.pixka.kt.pibase.d.MixedinfoRepo
import org.springframework.stereotype.Service

@Service
class MixedinfoService( var r: MixedinfoRepo) : DefaultService<Mixedinfo>() {
}