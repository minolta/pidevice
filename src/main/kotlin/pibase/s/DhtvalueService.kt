package me.pixka.pibase.s

import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.pibase.r.DhtvalueRepo
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class DhtvalueService(val r: DhtvalueRepo) : Ds<Dhtvalue>() {
    override fun search(search: String, page: Long, limit: Long): List<Dhtvalue>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun create(device: PiDevice, t: BigDecimal, h: BigDecimal, ip: String?, adddate: Date): Dhtvalue {
        var dv = Dhtvalue()
        dv.pidevice = device
        dv.pidevice_id = device.id
        dv.h = h
        dv.t = t
        dv.valuedate = adddate
        dv.ip = ip
        return save(dv)!!
    }

    fun last(): Dhtvalue? {
        // return dao.findTop1ByOrderByIdDesc();
        return r.findTop1ByOrderByValuedateDesc()
    }

    fun last(id: Long): Dhtvalue? {
        return r.findTop1ByPidevice_idOrderByValuedateDesc(id)
    }


    /**
     * ใช้สำหรับดึงข้อมูลจาก local ที่ยังไม่ส่งไปยัง Server toserver = false;
     *
     * @return
     */
    fun notInserver(): List<*>? {
        return r.findByToserver(false, topage(0, 500))
    }


    fun findValueforgraph(pideviceid: Long?, s: Date, e: Date): List<*>? {
        return r.findgraph(pideviceid, s, e)
    }

    fun cleanToserver() {
        r.deleteBySend()
    }
}
