package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Updatelocaldevices(val httpService: HttpService,val ps:PideviceService) {
    val om = ObjectMapper()
    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun updateDeviceinfo()
    {
        var target = System.getProperty("piserver")+"devices/"

        var result  = httpService.get(target)
        var localdevices =  ps.all()

        var list = om.readValue<List<PiDevice>>(result!!)

        localdevices.forEach {
            var found = findDevice(it,list)
            if(found!=null)
            {
                ps.save(found)
            }
        }


    }

    fun findDevice(device:PiDevice,fromnet:List<PiDevice>): PiDevice? {

        var found = fromnet.find { it.mac.equals(device.mac) }
        println(found)
        if(found!=null)
        {
            if(!device.ip.equals(found.ip) || !device.name?.equals(found.name)!!)
            {
                device.ip = found.ip
                device.name = found.name
                return device
            }

        }
        return null
    }
}