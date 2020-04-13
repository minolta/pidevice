package me.pixka.pibase.s

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.ktbase.io.Configfilekt
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.d.Job
import me.pixka.kt.pibase.d.Routedata
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

/**
 * ใช้สำหรับอ่านค่าต่างๆ ผ่าน Routedata
 *
 * @author kykub
 */
@Service
class ReadSensorService(val rs: RoutedataService,
                        val its: IptableServicekt,
                        val js: JobService,
                        val dhts: DhtvalueService,
                        val ds18s: Ds18valueService,
                        val http: HttpControl,
                        val cf: Configfilekt,var ps:PideviceService
                        ) {

    private val om = ObjectMapper()
    // now support 4 sensor
    private var HT: Job? = null
    private var DS: Job? = null
    private val port = "80"
    private var readbuffertimeout: String? = null // ใช้สำหรับบอกว่า
    // ถ้าอ่านข้อมูลจากเพื่อนไม่ได้ให้ใช้ค่าเดิมไปก่อนกี่ชั่วโมง
    private var timeout: Int = 0 // อายุของค่าสุดท้ายสำหรัวให้ระบบเก็บไว้
    private var lastds: Dslast? = null

    init {
        logger.info("readsensor constrnctor ")
    }

    @Throws(Exception::class)
    fun readDhtvalue(): Dhtvalue? {

        if (readbuffertimeout == null) {
            gettimeout()
        }
        if (HT == null)
            HT = findJob("HT")
        logger.debug("[readservice DHT] Job value:" + HT!!)

        if (HT == null) { // no job information
            logger.error("[readservice DHT] Job not found (HT)")
            throw Exception("Not have JOB HT information H DHT ")
        }

        try {
            val route = findRoute(HT!!)
            if (route != null) {

                val ip = desip(route)
                logger.debug("[readservice DHT] DES MAC to ip " + ip!!)
                if (ip != null) {
                    val url = "http://$ip:$port/dhtvalue"
                    val re = read(url)
                    logger.debug("[readservice DHT] $url return : $re")
                    if (re != null) {
                        try {
                            val value = om.readValue<Dhtvalue>(re, Dhtvalue::class.java!!)
                            logger.debug("[readservice DHTvalue DHT] read complete :" + value)
                            return value
                        } catch (e: IOException) {
                            logger.error("readservice Can not pars value to DHT :" + e.message)
                            e.printStackTrace()
                        }

                    } else {
                        logger.error("[readservice readdhtvalue DHT] error : " + re!!)
                    }

                } else {
                    logger.error("readservice readdhtvalue  not read other read local DHT value")
                }

            }

            val value = dhts!!.last()
            logger.debug("[readservice DHT] local :" + value)
            // ถ้าไม่มี route ก็อ่านจาก Local
            return value
        } catch (e: Exception) {
            logger.error("readservice  ReadDhtvalue() error " + e.message)
        }

        return null
    }

    @Throws(Exception::class)
    fun read(url: String): String? {
        logger.debug("[readservice read] Read from Other URL:" + url)
        try {
            logger.debug("readservice read from url: " + url)
            val re = http.get(url)
            logger.debug("[readservice read] Read value : " + re)
            return re
        } catch (e: Exception) {
            logger.error("[readservice read] Read error :  " + e.message + " URL: " + url)
            e.printStackTrace()
            throw e
        }

    }

    fun desip(route: Routedata): String? {

        try {
            val des = route.desmac
            val ip = its.findByMac(des!!)
            logger.debug("[readservice MAC to ip] find iptables object:" + ip!!)
            if (ip != null)
                return ip.ip
        } catch (e: Exception) {
            logger.error("[readservice desip] error:" + e.message)
        }

        return null

    }

    fun findRoute(job: Job): Routedata? {
        try {
            val r = rs.findbyJobId(job.id)
            logger.debug("[readservice findroute] find route found : " + r)
            return r
        } catch (e: Exception) {
            logger.error(" [readservice findroute] error :" + e.message)
            e.printStackTrace()
        }

        return null
    }

    fun findJob(h: String): Job? {
        try {

            val job = js.findTop1ByName(h)
            logger.debug("[readservice] JOB type for find pijob :" + job)
            return job
        } catch (e: Exception) {
            logger.error("[readservice findjob] error : " + e.message)
            e.printStackTrace()
        }

        return null
    }

    @Throws(Exception::class)
    fun readDSvalue(): DS18value? {
        logger.debug("[readservice readdsvalue] Start read DS Value ")
        if (readbuffertimeout == null) {
            gettimeout()
        }
        try {
            if (DS == null) {
                logger.debug("[readservice readdsvalue] Load main job DS :")
                DS = findJob("DS")
            } else {
                logger.debug("[readservice readdsvalue] Have DS :" + DS!!)
            }
        } catch (e: Exception) {
            logger.error("[readservice readdsvalue] Error JOB  DS  : " + e.message)
            throw Exception("Not have JOB DS information DS")
        }

        logger.debug("[readservice readdsvalue] Base Job value:" + DS!!)

        if (DS == null) { // no job information
            logger.error("[readservice readdsvalue] Job not found DS")
            throw Exception("Not have JOB information")
        }

        logger.debug("[readservice readdsvalue] Find route for " + DS!!)
        val route = findRoute(DS!!)
        if (route != null) {

            val ip = desip(route)
            logger.debug("[readservice readdsvalue] DES MAC to ip " + ip!!)

            if (ip != null) {
                var url: String? = null

                if (route.ds18sensor != null) {
                    // ถ้ามี sensor ระบบจะส่ง sensor ไปด้วย
                    val dss = route.ds18sensor
                    url = "http://" + ip + ":" + port + "/ds18valuebysensor/" + dss!!.name
                } else {
                    url = "http://$ip:$port/ds18value"
                }
                try {
                    logger.debug("[readservice readdsvalue] " + url)
                    val re = read(url)
                    logger.debug("[readservice readdsvalue] getvalue  " + re!!)
                    if (re != null) {
                        try {
                            val value = om.readValue<DS18value>(re, DS18value::class.java)
                            logger.debug("[readservice readdsvalue] read  other complete ds18b20 value :" + value)
                            updateDSTimeout(value, this.timeout.toLong())
                            return value
                        } catch (e: IOException) {
                            logger.error("[readservice readdsvalue] Error  :" + e.message)
                            e.printStackTrace()
                        }

                    } else {
                        logger.debug("[readservice readdsvalue] DS18B20 can not read from other device ("
                                + route.desmac + ") try to read local")
                    }
                } catch (e: Exception) {

                    logger.error("readservice read error : " + e.message)
                    e.printStackTrace()

                }

            }

        } else {
            logger.debug("[readservice useold readdsvalue] route not round for ds value read try to read local sensor")
        }

        if (lastds != null && checklastdstimeout()) {
            logger.debug("[readservice useold checklasdstimeout use old value] ")
            return lastds!!.value
        } else {
            logger.error("[readservice useold can not use old value] ")
        }

        var value: DS18value? = null
        try {
            value = ds18s!!.last()
            logger.debug("[readservice readdsvalue] Local value  :" + value!!)
        } catch (e: Exception) {
            logger.error("[readservice readdsvalue] " + e.message + " return NULL")
            e.printStackTrace()
        }

        // ถ้าไม่มี route ก็อ่านจาก Local
        logger.debug("[readservice readdsvalue] ds18b20 value : " + value!!)
        return value
    }

    private fun gettimeout() {
        try {
            this.readbuffertimeout = cf!!.getPropertie("valueexp")
            logger.debug("useold : time out : valueexp " + readbuffertimeout!!)
            if (readbuffertimeout != null)
                timeout = Integer.valueOf(readbuffertimeout!!)!!
        } catch (e: Exception) {
            logger.error("get timeout error useold : " + e.message)
        }

    }

    private fun checklastdstimeout(): Boolean {
        logger.debug("useold checktimeout ")
        try {
            if (lastds != null) {
                val n = Date().time
                val ex = lastds!!.timout
                logger.error("checklasdstimeout useold  n < ex  n:" + n + " ? ex :" + ex + " result : " + (ex - n))
                if (ex > n) {
                    logger.error("checklasdstimeout useold  can use old value")
                    return true
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("checklasdstimeout " + e.message)
        }

        logger.error("checklasdstimeout  can not use old value")

        return false
    }

    private fun updateDSTimeout(value: DS18value, timeout2: Long) {
        logger.debug("[readserver  useold updatetimeout ]")
        if (this.lastds == null) {
            lastds = Dslast()
            lastds!!.value = value
            lastds!!.timout = addH()// set timeout
            logger.debug("[readserver useold updatetimeout new last ds value ] " + lastds!!)
        } else {

            lastds!!.value = value
            lastds!!.timout = addH()
            logger.debug("[readserver  useold updatetimeout update value ] " + lastds!!)
        }

    }

    private fun addH(): Long {
        try {
            logger.debug("useold timeout value: " + timeout)
            val dt = Date()
            val dtOrg = DateTime(dt)
            val t = dtOrg.plusHours(timeout)
            return t.toDate().time
        } catch (e: Exception) {
            logger.error("useold add h error : " + e.message)
        }

        return 0
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ReadSensorService::class.java!!)
    }



}

internal class Dslast {
    var value: DS18value? = null
    var timout: Long = 0

    override fun toString(): String {
        return "Dslast [value=$value, timout=$timout]"
    }

}
