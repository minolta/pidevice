package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.pibase.r.Ds18valueRepo
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class Ds18valueService( val r: Ds18valueRepo) : DefaultService<DS18value>() {


    /**
     * ค่าสุดท้ายของ Device ที่ Save ไว้
     *
     * @return
     */
    fun last(): DS18value? {
        // return dao.findTop1ByOrderByIdDesc();
        return r.findTop1ByOrderByValuedateDesc()
    }

    fun create(pidevice: PiDevice?=null, t: BigDecimal?, valuedate: Date?=Date(), ip: String?=null): DS18value {
        val d = DS18value()
        d.ip = ip
        d.pidevice = pidevice
        d.valuedate = valuedate
        d.t = t
        return d

    }


    fun notInserver(): List<DS18value>? {

        return r.findTop500ByToserver(false)
    }

    fun findGraphvalue(piid: Long?, s: Date, e: Date): List<DS18value> {
        return r.findgraphvalue(piid, s, e) as List<DS18value>
    }

    fun last(id: Long?): DS18value? {
        return r.findTop1ByPidevice_idOrderByValuedateDesc(id)
    }

    fun lastBysensor(id: Long?): DS18value? {
        return r.findTop1ByDs18sensor_idOrderByValuedateDesc(id)
    }

    fun findGraphvalueBysensor(sid: Long?, s: Date, e: Date): List<DS18value>? {
        return r.findgraphvalueBySensor(sid, s, e)
    }

    fun cleanToserver()
    {
        r.cleanToserver()
    }
}
