package me.pixka.pibase.r

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import me.pixka.kt.pibase.d.Routedata

@Repository
interface RoutedataRepo : JpaRepository<Routedata, Long> {

    fun findBySrcmac(mac: String): List<*>?

    fun findByRefid(refid: Long?): Routedata?

    fun findByName(n: String): Routedata?

    fun findByJob_id(id: Long?): Routedata?

    fun findTop1ByJob_idOrderByIdDesc(id: Long?): Routedata?
    fun findTop1ByJob_idAndEnableOrderByIdDesc(id: Long?, e: Boolean?): Routedata?

}
