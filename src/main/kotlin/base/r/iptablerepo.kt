package me.pixka.kt.base.r

import me.pixka.kt.base.d.Iptableskt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
open interface IptablesktRepo : JpaRepository<Iptableskt, Long> {
    fun findByMac(mac: String): Iptableskt?
}