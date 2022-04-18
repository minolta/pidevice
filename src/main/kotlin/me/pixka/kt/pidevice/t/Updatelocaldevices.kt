package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class Updatelocaldevices(val httpService: HttpService,val ps:PideviceService) {
    val om = ObjectMapper()
    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    fun updateDeviceinfo()
    {
        var target = System.getProperty("piserver")+"devices/"

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI(target))
            .GET()
            .build()

        val response: HttpResponse<String> = HttpClient.newBuilder()
            .build()
            .send(request, HttpResponse.BodyHandlers.ofString())

        println(response.body())
        var list = om.readValue<List<PiDevice>>(response.body())
        var localdevices =  ps.all()
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