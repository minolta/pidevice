package me.pixka.kt.pidevice.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Sensorinjob
import me.pixka.kt.pibase.d.SensorinjobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.MactoipService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class LoadSensorinjobTask(val mtp: MactoipService,val ss:SensorinjobService,val ps:PijobService) {
    var om = ObjectMapper()
    var server = System.getProperty("piserver")
    var mac = System.getProperty("mac")


    @Scheduled(fixedDelay = 5000)
    fun load() {
        var re = mtp.http.get(server + "/getsensorbydevice/" + mac, 5000)
        var value = om.readValue<List<Sensorinjob>>(re)

        if(value!=null)
            value.forEach {
              var found =  ss.findByRefid(it.id)
                if(found==null)
                {
                    var pijob = ps.findByRefid(it.pijob_id)
                    it.refid=it.id //set refid
                    it.pijob = pijob
                    it.id = 0 //reset id for auto inc
                    ss.save(it)
                }

            }
    }
}