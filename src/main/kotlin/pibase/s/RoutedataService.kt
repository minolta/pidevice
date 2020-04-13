package me.pixka.pibase.s

import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Routedata
import me.pixka.pibase.r.RoutedataRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RoutedataService(val r:RoutedataRepo) : DefaultService<Routedata>() {




    /**
     * สำหรับให้ Server ส่งค่า Route ออกไปยัง client ที่ส่ง mac เข้ามา
     *
     * @param mac
     * @return
     */
    fun findByMac(mac: String): List<*>? {
        return r.findBySrcmac(mac)
    }

    fun findByRefid(refid: Long?): Routedata? {
        return r.findByRefid(refid)
    }

    fun searchMatch(n: String): Routedata? {
        return r.findByName(n)
    }

    fun findbyJobId(id: Long?): Routedata? {
        return r.findTop1ByJob_idAndEnableOrderByIdDesc(id, true)
    }

    fun create(d: Routedata): Routedata {

        val r = Routedata()
        r.refid = d.id
        r.description = d.description
        r.desmac = d.desmac
        r.srcmac = d.srcmac
        //r.setJob(d.getJob());
        r.name = d.name
        r.todo = d.todo
        return r
    }


}
