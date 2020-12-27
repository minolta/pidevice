package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Iptableskt
import me.pixka.kt.pidevice.o.NmapObject
import me.pixka.kt.pidevice.t.NewFindip
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.io.*

@DataJpaTest
class Npmxmltoobject {

    @Autowired
    lateinit var ips:IptableServicekt
    @Test
    fun test() {
        var file = System.getProperty("nmapfile")
        if (file == null) {
            logger.error("not set nmapfile -Dnmapfile=....")
            throw Exception("not set nmapfile -Dnmapfile=....")
        }

        var f = File(file)
        var devices = get(f)

        try {
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
        } catch (e: Exception) {
            logger.error("Save or edit ip has problem ${e.message}")
        }

        println(ips.all().size)

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