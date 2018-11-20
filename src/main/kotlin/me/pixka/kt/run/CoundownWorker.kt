package me.pixka.kt.run

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * เป็นตัว run การนับว่าจะให้ทำอะไร
 */
class CoundownWorkerii(var pijob: Pijob, var gpios: GpioService, val sensorService: SensorService) : PijobrunInterface, Runnable {

    var runtime: Int = 0
    var isRun = false
    var startDate: Date? = null
    val d = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val dn = SimpleDateFormat("yyyy/MM/dd")

    override fun run() {
        if (!checkincondition()) {
            logger.error("Not in rang end this job")
            return
        }
        isRun = true
        setup()
        startDate = Date()

        var timeout = 0
        var canrunport = false
        //start run
        while (runtime > 0) {


            if (!checkincondition()) {
                timeout++
                if (timeout >= 600) // ถ้าไม่อยู่ในเงือนไข 10 นาที ก็จบการทำงานเลย
                {
                    logger.error("Error Timeout 10 min")
                    isRun = false
                    break
                }
            }
            else
            {
                timeout = 0;
            }
            runtime--
            TimeUnit.SECONDS.sleep(1)
            if (runtime <= 0) {//ถ้า นับมาจนครบ ก็เริ่มทำงาน
                canrunport = true
                logger.info("To run port")
                break
            }

        }


        if (canrunport) {
            runport()
        }



        logger.info("End countdown")

    }

    /**
     * การ runport จะเปิด port ตามกำหนด และ
     * ใช้ค่า  waittime สำหรับรอ
     */
    fun runport() {

        //ถ้ามีการกำหนด เวลา เริ่มต้น
        if (pijob.stime != null || pijob.stimes != null) {

            checktime() // รอจนกว่าจะถึงเวลาเริ่มทำงาน
        }


        var runtime = 1

        if (pijob.timetorun != null && pijob.timetorun!!.toInt() > 0)
            runtime = pijob.runtime!!.toInt()


        var portlist = pijob.ports

        if (portlist != null) {
            while (runtime > 0) {
                runtime--

                for (port in portlist) {
                    var pin = gpios.getDigitalPin(port.portname?.name!!)
                    var logic = port.status!!.name
                    var l = true
                    if (logic!!.toLowerCase().indexOf("false") != -1) {
                        l = false
                    }
                    gpios.setPort(pin!!, l)
                    TimeUnit.SECONDS.sleep(pijob.waittime!!)
                    gpios.setPort(pin!!, !l)

                }


            }
        }

    }

    fun checktime() {
        if (pijob.stimes != null) {

            var timetorun = getnextrunt()

            while (true) {
                var now = Date().time

                logger.debug("cooldown checktime checktime nextrun time wait time :  ${timetorun!!.time} now ${now}")
                if (now.toInt() >= timetorun.time.toInt()) {
                    break
                }

                TimeUnit.SECONDS.sleep(1)
            }
        }
    }

    fun getnextrunt(): Date? {
        try {
            var ds = dn.format(Date())
            println("DS: ${ds}")
            var datenow = dn.parse(ds)
            val c = Calendar.getInstance()
            c.time = datenow
            c.add(Calendar.DATE, 1)  // number of days to add
            var nextdate = dn.format(c.time)
            var timetorun = d.parse(nextdate + " " + pijob.stimes)
            return timetorun
        } catch (e: Exception) {
            logger.error("coundown getnextrun pares date ${e.message}")
        }
        return null
    }

    //ใช้สำหรับอ่านค่า ว่าอยู่ในช่วงหรือเปล่า
    fun checkincondition(): Boolean {
        if (pijob != null) {
            if (pijob.desdevice_id != null && pijob.ds18sensor_id != null) {
                return readother()
            } else if (pijob.desdevice_id != null) {
                return readktype()
            }


        }
        return false
    }

    fun readktype(): Boolean {
        var value = sensorService.readDsOther(pijob.desdevice_id!!, null)
        if (value == null || value.t == null)
            return false

        if (value != null) {
            var v = value.t!!.toFloat()
            var tl = pijob.tlow!!.toFloat()
            val th = pijob.thigh!!.toFloat()

            if (v >= tl && v <= th)
                return true
            return false

        }
        return false
    }

    fun readother(): Boolean {
        var value = sensorService.readDsOther(pijob.desdevice_id!!, pijob.ds18sensor_id)
        if (value == null || value.t == null)
            return false

        if (value != null) {
            var v = value.t!!.toFloat()
            var tl = pijob.tlow!!.toFloat()
            val th = pijob.thigh!!.toFloat()

            if (v >= tl && v <= th)
                return true
            return false

        }
        return false
    }

    fun setup() {

        if (pijob != null && pijob.runtime != null) {
            runtime = pijob.runtime!!.toInt()
        } else {
            isRun = false
            throw Exception("No runtime information")
        }


    }


    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    override fun setG(gpios: GpioService) {
        this.gpios = gpios
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun getPijobid(): Long {
        if (pijob != null) {
            return pijob.id
        }
        return 0L
    }

    override fun getPJ(): Pijob {
        return pijob
    }

    override fun startRun(): Date? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun state(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CoundownWorkerii::class.java)
    }

}