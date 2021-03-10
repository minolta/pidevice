package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pidevice.o.NmapObject
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File

/**
 * สำหรับ อ่านค่า scan ip จาก file แล้วเอามาใส่ใน Database
 */
@Component
@Profile("!test,ip")
class NewFindip(val ips: IptableServicekt) {

    @Scheduled(fixedDelay = 60000)
    fun readfile() {
        var file = System.getProperty("nmapfile")
        if (file == null) {
            logger.error("not set nmapfile -Dnmapfile=....")
//            throw Exception("not set nmapfile -Dnmapfile=....")
//            return
        }
        else {

            var f = File(file)
            var devices = get(f)

            try {
                if (devices.size > 0) {

                    devices.forEach {

                        var ip = ips.findByMac(it.mac!!)
                        if (ip != null) {//edit
                            ip.ip = it.ip
                            ips.save(ip)
                        } else {//new
                            ip = Iptableskt()
                            ip.mac = it.mac
                            ip.ip = it.ip
                            ips.save(ip)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Save or edit ip has problem ${e.message}")
            }
        }
    }

    @Scheduled(cron = "00 00 00 * * ?")
    fun clear() {
        ips.deleteALL()
    }

    fun get(f: File): ArrayList<NmapObject> {
        var devices = ArrayList<NmapObject>()
        var n = NmapObject()

        f.forEachLine {

//           println(it)
            if (it.startsWith("Nmap scan")) {
                var p = it.split(" ")
                println(p[4])
                n.ip = p[4]
            }
            if (it.startsWith("MAC Address")) {
                var m = it.split(" ")
                println(m[2])
                n.mac = m[2]
                devices.add(n)
                n = NmapObject()
            }
        }

        return devices
    }

    var logger = LoggerFactory.getLogger(NewFindip::class.java)
}