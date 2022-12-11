package me.pixka.kt.pidevice.d

import jakarta.persistence.Entity
import me.pixka.base.d.En
import me.pixka.base.s.DefaultService
import me.pixka.base.s.findByName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Entity
class Configdata(var name: String? = null, var valuename: String? = null) : En() {
    override fun toString(): String {
        return "${name}:${valuename}"
    }
}

@Repository
interface ConfigdataRepo : JpaRepository<Configdata, Long>, findByName<Configdata> {

}

@Service
class ConfigdataService(val r: ConfigdataRepo) : DefaultService<Configdata>() {

    @Synchronized
    fun findOrCreate(n: String, value: String? = null): Configdata {
        var f = r.findByName(n)
        if (f == null) {
            f = Configdata(n, value)
            return save(f)
        }

        return f
    }

    fun getValue(n:String,defaultvalue:String?=null): Configdata {
        var value = findOrCreate(n,defaultvalue)
        return value
    }
}