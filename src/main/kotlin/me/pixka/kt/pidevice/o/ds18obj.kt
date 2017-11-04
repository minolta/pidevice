package me.pixka.kt.pidevice.o

import me.pixka.kt.pidevice.t.Dssensorforfindlast
import me.pixka.kt.pidevice.t.FindJobforRunDS18value
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.Ds18valueService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile("pi")
class DS18obj(var dss: DS18sensorService, var dsvs: Ds18valueService) {
    /*ใช้สำหรับหาค่าสุดท้ายของ Sensor แต่ละตัว*/
    fun findlast(): ArrayList<Dssensorforfindlast> {
        var buf = ArrayList<Dssensorforfindlast>()
        //หาข้อมูลจาก Last
        var allsensor = dss.search("", 0, 1000)
        if (allsensor != null)
            for (o in allsensor) {


                var t = dsvs.lastBysensor(o.id)
                if (t != null) {
                    var dsfl = Dssensorforfindlast(o, t)
                    buf.add(dsfl)
                }
            }

        FindJobforRunDS18value.logger.info("Last T by sensor ${buf}")
        return buf
    }
}
