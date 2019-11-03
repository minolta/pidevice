package me.pixka.kt.pidevice.d

import me.pixka.kt.base.d.En
import me.pixka.kt.pidevice.s.DefaultService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.Entity

@Entity
class Countfg(var mac: String? = null, var count: Long? = null, var odd: Long? = null, var ip: String? = null,
              var toserver:Boolean=false) : En()

@Repository
interface CountfgRepo : JpaRepository<Countfg, Long>
{
    fun findByToserver(b:Boolean):ArrayList<Countfg>?
}

@Service
class CountfgService(val r:CountfgRepo) : DefaultService<Countfg>()
{
    fun nottoserver()=r.findByToserver(false)

}