package me.pixka.kt.pibase.c


import me.pixka.kt.base.s.DbconfigService
import me.pixka.kt.base.s.ErrorlogService
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.pibase.s.DS18sensorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


/**
 * ใช้สำหรับติดต่อกับ H/w ของ PI
 *
 * @author kykub
 */
@Service
//@Profile("pi", "lite","server")
class Piio(val dbcfg: DbconfigService, val ds18s: DS18sensorService, val err: ErrorlogService) {
    /**
     * ใช้สำหรับดึง MAC address ของ WIFI
     *
     * @return
     * @throws IOException
     */


    var dhtDirPath = "/sensor/dht22"

    var dhtTvalue: BigDecimal? = null
//        private set
    var dhtHvalue: BigDecimal? = null
//        private set
    private val ds18value: BigDecimal? = null

    @Throws(IOException::class)
    fun wifiMacAddress(): String {
        try {
            var path = "/sys/class/net/wlan0/address"
            var contents = String(Files.readAllBytes(Paths.get(path)))
            contents = contents.replace(" ".toRegex(), "")
            contents = contents.replace("\n".toRegex(), "")
            contents = contents.replace("\r".toRegex(), "")
            return contents
        } catch (e: Exception) {
            logger.error("Piio ${e.message}")
            var mac = System.getProperty("mac")
            if (mac != null) {
                logger.debug("Have mac ${mac}")
                return mac
            }
        }
        return "error"
    }

    fun readDHT(): Dhtvalue? {

        loadpath()

        var content: String

        try {
            content = String(Files.readAllBytes(Paths.get(dhtDirPath)))
            logger.debug("DHT22 Raw value :" + content)
            content = content.replace(" ".toRegex(), "").replace("(\\r|\\n)".toRegex(), "")
            val value = content.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            logger.debug("[" + value[0] + "]----[" + value[1] + "]")
            if (value.size > 0) {
                this.dhtTvalue = BigDecimal(value[0])
                this.dhtHvalue = BigDecimal(value[1])
                val d = Dhtvalue()
                d.t = dhtTvalue
                d.h = dhtHvalue
                d.ip = wifiIpAddress()
                d.valuedate = Date()
                d.toserver = false
                return d
            }
        } catch (e: Exception) {

            err.n("Piio", "65-79", "${e.message}")
            logger.error("Canot read DHT22 ${e.message}")
            this.dhtTvalue = null
            this.dhtHvalue = null
        }

        return null
    }

    /**
     * ใช้ load config ถ้ามีการกำหนดก็จะเปลียน ตำแหน่งอ่านค่าของ dht22
     */
    private fun loadpath() {
        dhtDirPath = dbcfg.findorcreate("dhtpath", "/sensor/dht22").value!!
    }

    /**
     * IP สำหรับ WL lan
     *
     * @return
     */
    fun wifiIpAddress(): String? {

        try {
            var path = "/sensor/wlan"
            var content: String? = String(Files.readAllBytes(Paths.get(path)))
            content = content?.replace(" ".toRegex(), "")?.replace("(\\r|\\n)".toRegex(), "")
            return content
        } catch (e: Exception) {
            logger.error(e.message)
            var fixip = System.getProperty("fixip")

            if (fixip != null)
                return fixip

            val inetAddress = InetAddress.getLocalHost()
            println("IP Address:- " + inetAddress.hostAddress)
            println("Host Name:- " + inetAddress.hostName)
            return inetAddress.hostAddress.toString()
        }

    }

    /**
     * Local read DS18B20
     *
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun readDs18(): BigDecimal {
        // logger.debug("DS18b20 read");
        val dir = File(w1DirPath)
        val files = dir.listFiles(DirectoryFileFilter())
        if (files != null) {
            logger.debug("[ds18b20] found devices " + files.size)
            // while (true) {
            for (file in files) {
                // System.out.print(file.getName() + ": ");
                // Device data in w1_slave file
                val filePath = w1DirPath + File.separatorChar + file.name + File.separatorChar + "w1_slave"
                val f = File(filePath)
                try {

                } catch (ex: Exception) {
                    logger.error("[ds18b20 readio] error " + ex.message)
                }

            }
        }
        // }

        throw Exception("Canot read ds18b20")
    }

    val b1000 = BigDecimal("1000")
    @Throws(Exception::class)
    fun reads(): ArrayList<DS18value>? {
        // logger.debug("DS18b20 read");
        val buf = ArrayList<DS18value>()
        val dir = File(w1DirPath)
        val files = dir.listFiles(DirectoryFileFilter())

        if (files != null) {
            logger.debug("[ioreaddsvalue] found sensors : " + files.size)
            // while (true) {
            for (file in files) {
                // System.out.print(file.getName() + ": ");
                // Device data in w1_slave file
                val filePath = w1DirPath + File.separatorChar + file.name + File.separatorChar + "w1_slave"
                val f = File(filePath)
                try {
                    val inputStream: InputStream = f.inputStream()
                    inputStream.bufferedReader().useLines { lines ->
                        lines.forEach {
                            val index = it.indexOf("t=")
                            if (index != -1) {
                                var value = it.substring(it.indexOf("t=") + 2)
                                var bv = BigDecimal(value).divide(b1000, 2, RoundingMode.HALF_UP)
                                var d = DS18value()
                                d.t = bv
                                val ss = ds18s.findorcreate(file.name)
                                d.ds18sensor = ss
                                d.valuedate = Date()
                                buf.add(d)

                            }
                        }
                    }


                } catch (ex: Exception) {
                    logger.error(ex.message)
                    // err.n("Piio", "198-214", "${ex.message}")
                }

            }

            return buf
        }
        // }
        logger.error("Read ds18b20 error")
        throw Exception("Canot read ds18b20")
    }

    fun getPidevice(): PiDevice {
        var p = PiDevice()
        p.mac = wifiMacAddress()
        return p
    }

    fun readDs18(sensorname: String): BigDecimal? {
        var values = reads()

        if (values != null) {
            for (v in values) {
                if (v.ds18sensor?.name.equals(sensorname)) {
                    return v.t
                }
            }
        }

        return null
    }

    fun readDs18value(sensorname: String): DS18value? {
        var values = reads()

        if (values != null) {
            for (v in values) {
                if (v.ds18sensor?.name.equals(sensorname)) {
                    return v
                }
            }
        }

        return null
    }

    internal inner class DirectoryFileFilter : FileFilter {
        override fun accept(file: File): Boolean {
            val dirName = file.name
            val startOfName = dirName.substring(0, 3)
            return file.isDirectory && startOfName == "28-"
        }
    }

    companion object {

        internal var logger = LoggerFactory.getLogger(Piio::class.java)
        internal var w1DirPath = "/sys/bus/w1/devices"
    }

}
