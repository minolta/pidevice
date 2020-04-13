package me.pixka.pibase.r

import me.pixka.kt.base.s.search
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.RestController

import me.pixka.kt.pibase.d.Devicecheckin

@RestController
interface DevicecheckinRepo : JpaRepository<Devicecheckin, Long> {
    fun findTop1ByPidevice_idOrderByIdDesc(id: Long): Devicecheckin?
}
