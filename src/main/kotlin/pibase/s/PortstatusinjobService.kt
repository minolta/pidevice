package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.pibase.r.PortstatusinjobRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PortstatusinjobService( val r: PortstatusinjobRepo) : DefaultService<Portstatusinjob>() {



    fun searchMatch(n: String): Portstatusinjob? {
        return null
    }

    fun findByPijobid(pijobid: Long?): List<*>? {
        return r.findByPijob_id(pijobid)
    }

    fun clear() {
        repo.deleteAll()
    }

    fun finByRefId(refid: Long?): Portstatusinjob? {
        if (refid == null) {
            logger.error("[portstatusinjobservice finByRefId] error REFID is null : " + refid!!)
            return null
        }
        try {
            return r.findByRefid(refid)
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("[portstatusinjobservice finByRefId] error : " + e.message)
        }

        return null
    }

    fun deleteBypideviceId(id: Long): Boolean {
        r.deleteByPijobId(id)
        return true
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(PortstatusinjobService::class.java!!)
    }

}
