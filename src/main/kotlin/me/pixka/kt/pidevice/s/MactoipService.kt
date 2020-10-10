package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import org.springframework.stereotype.Service
import java.util.*

@Service
class MactoipService(val ips:IptableServicekt,val lgs:LogService,val http:HttpService,
                     val dhts:ReadDhtService)
{

    fun mactoip(mac:String): String? {

        try{
            var ip = ips.findByMac(mac)

            if(ip!=null)
                return ip.ip
            lgs.createERROR("IP Not found for ${mac}", Date(),"MacToip","",
            "14","mactoip","${mac}")
            throw Exception("IP Not found for ${mac}")
        }
        catch (e:Exception)
        {
            lgs.createERROR("${e.message}", Date(),"MacToip","",
                    "14","mactoip","${mac}")
            throw e
        }


    }
}