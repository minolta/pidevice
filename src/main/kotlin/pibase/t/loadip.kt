package me.pixka.kt.pibase.t

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.base.s.IptableServicekt
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.concurrent.TimeUnit

//@Profile("ip")
@Component
class Iptask(val service: IptableServicekt, val cfg: DbconfigService, val es: ErrorlogService) {
    companion object {
        internal var logger = LoggerFactory.getLogger(Iptask::class.java)
    }

    var s: String? = null
    var command: String? = "C:\\nmap.exe -n -sP "


    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun loadip() {
        logger.debug("Scan ip ${Date()}")
        setup()
        logger.debug("starttask run IPTABLES")
        var result = r()
        logger.debug("Return value :${result}")


        if (result != null) {
            savetoDB(result)
            logger.debug("Save  iptables ${result}")
        } else {
            logger.error("Can not save ${result}")
        }


    }


    fun readline(std: BufferedReader): String? {
        s = std.readLine()
        logger.debug("Read line : ${s}")
        return s
    }


    fun setup() {
        // logger.debug("Set ups")
        var p = System.getProperty("scancommand")

        if (p != null) {
            command = "${p} -n -sP "
        } else
            command = cfg.findorcreate("scancommand", "C:\\nmap.exe -n -sP ").value!!


    }

    fun editIptable(indevice: Iptableskt, it: Iptableskt) {
        var i = indevice
        try {
            i.ip = it.ip
            i.lastcheckin = it.lastcheckin
            i = service.save(i)!!
            logger.debug("Update iptable : ${i}")
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("loadiptable editiptable() " + e.message)
        }

    }

    fun saveMeip(ip: String) {
        logger.debug("iptable  save own ip ${ip}")
        val out = PrintWriter("/sensor/wlan")
        out.print(ip)
        out.close()


    }

    var meip: Ip? = null
    fun getNet(): ArrayList<String> {
        var buffer = ArrayList<String>()
        try {
            var nw = network()
            logger.info("Network is ")
            for (n in nw) {
                //var ipdata = n?.split(",")
                val n = n.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val nn = n[0] + "." + n[1] + "." + n[2] + ".0/24"
                logger.debug("Network ${nn}")
                buffer.add(nn)
            }
        } catch (e: Exception) {
            logger.error("getNet ${e.message}")
            es.n("load ip ", "110", "${e.message}")
            //  e.printStackTrace()
        }
        return buffer
    }

    fun findIP(network: String, cmd: String): ArrayList<Ip> {
        var c = cmd + " " + network
        logger.debug("Scan command ${c}")

        if (System.getProperty("scan1") != null)
            c = System.getProperty("scan1")

        val proc = Runtime.getRuntime().exec(c)
        TimeUnit.SECONDS.sleep(10)
        val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
        // read the output from the command
        val buf = ArrayList<Ip>()
        var ip: Ip? = null
        while (readline(stdInput) != null) {
            logger.debug("================>${s}")
            if (s != null) {
                if (s!!.startsWith("Nmap scan")) {
                    if (ip == null)
                        ip = Ip()
                    val g = s!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    ip.ip = g[4]
                    logger.debug("Save ip ${ip.ip}")
                }

                if (s!!.startsWith("MAC Address:")) {
                    val g = s!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    ip!!.mac = g[2]
                    buf.add(ip)
                    logger.debug("Save mac ${ip.mac}")
                    ip = null
                }
            }

        }


        if (meip != null)
            buf.add(meip!!) //เพิ่มตัวเอง
        return buf


    }

    fun r(): ArrayList<Ip>? {

        var buf = ArrayList<Ip>()
        try {
            var network = getNet()


            for (n in network) {
                var b = findIP(n, command!!)
                buf.addAll(b)
            }

        } catch (e: Exception) {
            logger.error("R() ${e.message}")
            es.n("Loadip", "180", "${e.message}")
            e.printStackTrace()
        }
        return buf
    }

    fun savetoDB(buf: ArrayList<Ip>) {
        try {
            for (i in buf) {
                //  logger.debug("find iptables : ${i}")
                if (i != null && i.mac != null) {

                    //      logger.debug("I for find ADDRESS: ${i.mac} Service is ${service}")

                    var mac = i.mac
                    //      logger.debug("mac value : ${mac}")
                    var ii: Iptableskt? = service.findByMac(mac!!)
                    //      logger.debug("Address  Found: ${ii}")
                    if (ii == null) {
                        ii = Iptableskt()
                        ii.ip = i.ip
                        ii.mac = i.mac
                        newIptable(ii)
                    } else {
                        service.updateiptable(ii, i.ip!!)
                    }
                }
            }


            buf.clear()
            logger.debug("Clear buffer : ${buf} buf size: ${buf.size}")
        } catch (e: Exception) {
            logger.debug("Error in for : ${e}")
            e.printStackTrace()
        }
    }

    @Throws(SocketException::class)
    fun network(): ArrayList<String> {
        var buffer = ArrayList<String>()
        try {
            val e = NetworkInterface.getNetworkInterfaces()
            while (e.hasMoreElements()) {
                val n = e.nextElement() as NetworkInterface
                val ee = n.inetAddresses
                while (ee.hasMoreElements()) {
                    val i = ee.nextElement() as InetAddress
                    //  logger.debug("loadiptable Internet Address ${i.address} ${n.hardwareAddress}")
                    if (i.hostAddress.startsWith("192") || i.hostAddress.startsWith("10")) {


                        var mac = getMacAddress(i)
                        if (mac != null)
                            buffer.add("${i.hostAddress},${mac}")
                        //return "${i.hostAddress},${mac}"
                    }
                }

            }
        } catch (e: Exception) {
            logger.error("loadiptable network() !! ${e.message}")
        }
        return buffer
    }

    fun getMacAddress(ip: InetAddress): String? {
        var address: String? = null
        try {

            val network = NetworkInterface.getByInetAddress(ip)
            val mac = network.hardwareAddress

            val sb = StringBuilder()
            for (i in mac.indices) {
                sb.append(String.format("%02X%s", mac[i], if (i < mac.size - 1) ":" else ""))
            }
            address = sb.toString()

        } catch (ex: SocketException) {

            ex.printStackTrace()

        }

        return address
    }


    fun newIptable(it: Iptableskt) {
        try {
            var idv = Iptableskt()
            idv.ip = it.ip
            idv.mac = it.mac
            idv.lastcheckin = Date()
            idv = service.save(idv)!!
            logger.debug("loadiptable New iptables : ${idv}")
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("loadpitable newiptable() error: " + e.message)
        }

    }


}

class Ip {
    var mac: String? = null
    var ip: String? = null

    override fun toString(): String {
        return "Ip [mac=$mac, ip=$ip]"
    }
}
