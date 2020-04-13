package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.pibase.d.Jobatpi
import me.pixka.pibase.r.JobatpiRepo
import org.springframework.stereotype.Service

@Service
class JobatpiService(var r: JobatpiRepo) : DefaultService<Jobatpi>() {

    fun findbyMac(mac: String): List<*>? {
        return null
    }

    fun findByPiID(id: Long?): List<*>? {
        return r.findByPidevice_id(id)
    }

}
