package me.pixka.pibase.r

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Pidevicegroup

@Repository
interface PidevicegroupRepo : JpaRepository<Pidevicegroup, Long> {

    fun findByName(name: String): Pidevicegroup?

}
