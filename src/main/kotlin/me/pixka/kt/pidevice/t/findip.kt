package me.pixka.kt.pidevice.t

import me.pixka.kt.base.d.Iptableskt
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.ktbase.io.Configfilekt
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


@Component
//@Profile("pi")
class Findip(val service: IptableServicekt, val cfg: Configfilekt, val ntfs: NotifyService) {
    companion object {
        internal var logger = LoggerFactory.getLogger(Findip::class.java)
    }

    var s: String? = null
    var command: String? = "nmap -n -sP "


    @Scheduled(fixedDelay = 5000)
    fun loadip() {
        logger.info("Scan ip ${Date()}")
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
        // logger.debug("Read line : ${s}")
        return s
    }


    fun setup() {
        // logger.debug("Set ups")
        command = "nmap -n -sP"
        //dbcfg.findorcreate("nmap", "nmap -n -sP").value
    }


    var meip: Ip? = null
    fun getNet(): ArrayList<String> {
        var buffer = ArrayList<String>()
        try {
            var nw = network()
            for (n in nw) {
                var ipdata = n.split(",")
                val n = n.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val nn = n[0] + "." + n[1] + "." + n[2] + ".0/24"
                buffer.add(nn)
            }
        } catch (e: Exception) {
            logger.error("getNet ${e.message}")
//            es.n("load ip ", "110", "${e.message}")
            //  e.printStackTrace()
        }
        return buffer
    }

    fun findIP(network: String, cmd: String): ArrayList<Ip> {
        val c = cmd + " " + network
        val proc = Runtime.getRuntime().exec(c)
        logger.debug("Command is ${c}")
        val stdInput = BufferedReader(InputStreamReader(proc.inputStream))
        // read the output from the command
        val buf = ArrayList<Ip>()
        var ip: Ip? = null
        while (readline(stdInput) != null) {
            if (s != null) {
                if (s!!.startsWith("Nmap scan")) {
                    if (ip == null)
                        ip = Ip()
                    val g = s?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                    ip.ip = g!![4]
                }

                if (s!!.startsWith("MAC Address:")) {
                    val g = s?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

                    ip!!.mac = g!![2]
                    buf.add(ip)
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
            logger.debug("Network found ${network}")

//            network.map {
//                findIP(it,command!!)
//            }
            for (n in network) {
                var b = findIP(n, command!!)
                logger.debug("Ip found ${b}")
                buf.addAll(b)
            }

        } catch (e: Exception) {
            logger.error("R() ${e.message}")
//            es.n("Loadip", "180", "${e.message}")
            e.printStackTrace()
        }
        return buf
    }

    fun savetoDB(buf: ArrayList<Ip>) {

       buf.map {
            var oldip = service.findByMac(it.mac!!)
            if (oldip != null) {
                //edit
                oldip.ip = it.ip
                oldip.lastupdate = Date()
                oldip = service.save(oldip)
                logger.debug("Update ${oldip?.devicename}  TO IP ${oldip?.ip} MAC:${oldip?.mac}")

            } else {
                var newip = Iptableskt()
                newip.mac = it.mac
                newip.ip = it.ip
                newip.lastupdate = Date()
                newip.adddate = Date()

                var np = service.save(newip)!!
                logger.debug("New Ip address MAC:${np.mac} IP:${np.ip}")
            }
        }
//        try {
//            for (i in buf) {
//                //  logger.debug("find iptables : ${i}")
//                if (i.mac != null) {
//
//                    //      logger.debug("I for find ADDRESS: ${i.mac} Service is ${service}")
//
//                    var mac = i.mac
//                    //      logger.debug("mac value : ${mac}")
//                    var ii: Iptableskt? = service.findByMac(mac!!)
//                    //      logger.debug("Address  Found: ${ii}")
//                    if (ii == null) {
//                        ii = Iptableskt()
//                        ii.ip = i.ip
//                        ii.mac = i.mac
//                        newIptable(ii)
//                    } else {
//                        service.updateiptable(ii, i.ip!!)
//                    }
//                } else {
//                    logger.info("Saveme: ${i}")
//                    //me device
//                    var ii: Iptableskt? = service.findByMac("")
//                    //      logger.debug("Address  Found: ${ii}")
//                    if (ii == null) {
//                        ii = Iptableskt()
//                        ii.ip = i.ip
//                        ii.mac = i.mac
//                        newIptable(ii)
//                    } else {
//                        service.updateiptable(ii, i.ip!!)
//                    }
//
//                }
//            }
//
//
//            buf.clear()
//            logger.debug("Clear buffer : ${buf} buf size: ${buf.size}")
//        } catch (e: Exception) {
//            logger.debug("Error in for : ${e}")
//            e.printStackTrace()
//        }
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
