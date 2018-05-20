package me.pixka.kt.run

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


@Profile("pi", "lite")
class CDWorker(var pj: Pijob, val ss: SensorService, var gi: GpioService,
               val m: MessageService, val i: Piio, val ppp: PortstatusinjobService, val pjs: PijobService) : Worker(pj, gi, i, ppp) {
    var df: DateFormat = SimpleDateFormat("HH:mm:ss")
    val d = SimpleDateFormat("yyyy/mm/dd HH:mm")
    val dn = SimpleDateFormat("yyyy/mm/dd")
    override fun run() {
        try {
            isRun = true
            startrun = Date()
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
            var checkports = findcheckport(ports)

            var items = ports.iterator() as MutableList<Portstatusinjob>
            //remove check item
            for(item in items)
            {
                if(item.portname?.name?.toLowerCase().equals("check"))
                {
                    items.remove(item)
                }
            }
            ports = items as List<Portstatusinjob>
            logger.debug("port size ${checkports.size} : ${items.size}")


            var runtime = pijob.runtime
            var waittime = pijob.waittime
            jobid = pijob.id
            var loop = 1
            if (pijob.timetorun != null) {
                if (pijob.timetorun!!.toInt() > 0)
                    loop = pijob.timetorun!!.toInt()
            }
            logger.debug("Startworker CDWorker ${pijob.id}")
            m.message("Start Check High tmp", "cooldowninfo")
            this.state = "Start check High tmp"
            //  ms.message("Start work id:${pijob.id} ", "info")


            //เริ่มการหายอดสูงสุด
            while (true) {

                var value = readDs()
                if (value == null) {
                    logger.error("Can not read DS18value in cooldown worker")
                    state = "Can not read high tmp"
                    TimeUnit.SECONDS.sleep(10000)
                    continue
                }
                logger.debug("High tmp check ${value}")
                if (value.toInt() > pijob.thigh!!.toInt()) {
                    //รอสองนาที
                    m.message("High tmp is over and wait 120 sec", "cooldownrun")
                    logger.debug("High tmp is ok and wait 120 sec to check again")
                    state = "Hi tmp is ok check again"
                    TimeUnit.SECONDS.sleep(120)

                    value = readDs()
                    if (value == null) {
                        logger.error("Can not read DS18value in cooldown worker")
                        state = "Can not read high tmp"
                        TimeUnit.SECONDS.sleep(10000)
                        continue
                    }
                    if (value.toInt() > pijob.thigh!!.toInt()) {
                        m.message("Check High tmp is ok have to wait low tmp", "cooldownrun")
                        logger.debug("High tmp is ok next check lowtmp")
                        state = "Tmp is ok go to check low tmp"
                        break //ถ้าความร้อนถึงแล้วเข้าสู่ mode รอให้ ความร้อนลงตำสุดก่อน
                    } else {
                        logger.error("High tmp not ready to next")
                    }
                }

                logger.error(" DS18value ${value} < ${pijob.thigh} in cooldown worker")
                state = "still high tmp wait to check again Date ${Date()}"
                TimeUnit.SECONDS.sleep(10)
            }

            state = " start check low tmp"
            //รอจนกว่า ความร้อนจะลงตำสุดระบบจะเริ่มทำงาน
            while (true) {
                var value = readDs()
                if (value == null) {
                    m.message("Can not read Low tmp", "cooldownrun")
                    logger.error(" DS18value Canon read low tmp")
                    state = " can not read low tmp"
                    continue
                }
                logger.debug("Low tmp check ${value}")
                state = "low tmp is ${value}"
                if (value!!.toInt() < pijob.tlow!!.toInt()) {
                    //รอสองนาที
                    m.message("Low tmp is under and wait 120 sec", "cooldownrun")
                    state = "Low tmp is under and wait 120 sec"
                    TimeUnit.SECONDS.sleep(120)

                    value = readDs()
                    if (value!!.toInt() < pijob.tlow!!.toInt()) {
                        m.message("Check Low tmp is ok have to run this job ${pijob}", "cooldownrun")
                        logger.debug(" Tmp is low condition run this job cooldownrunthisjob")
                        state = "Now low tmp is ok go to run jobs"
                        break //ถ้าความร้อนถึงแล้วเข้าสู่ mode รอให้ ความร้อนลงตำสุดก่อน
                    } else {
                        logger.error("Low  tmp not ready to run cooldown")
                        state = "low tmp not ready to run"
                    }
                }


                logger.error(" DS18value ${value} > ${pijob.tlow} in cooldown worker")
                state = "Low tmp is ${value} > ${pijob.tlow}"
                TimeUnit.SECONDS.sleep(10)

            }


            if (pijob.stimes != null) {
                state = "Have stime this job have to start after ${pijob.stimes}"

                var ds = dn.format(Date())
                var datenow = d.parse(ds)
                val c = Calendar.getInstance()
                c.time = datenow
                c.add(Calendar.DATE, 1)  // number of days to add
                var nextdate = dn.format(c.time)
                var timetorun = d.parse(nextdate + " " + pijob.stimes)

                while (true) {
                    var now = Date().time
                    state = "Next run ${timetorun.time} now ${now}"
                    if (now.toInt() >= timetorun.time.toInt()) {
                        state = "exit time check  Start run now"
                        break
                    }

                    state = "Not have time to run"
                    TimeUnit.SECONDS.sleep(10000)
                }
            }

            var i = 0

            logger.debug("cooldown loopis ${loop}")

            while (i < loop) {

                var value = 1
                if (checkports.size > 0) {

                    var port = checkports[0].portname
                    var p = gpio.readPort(port?.name!!)
                    if (p!!.isHigh) {
                        value = 1
                    } else
                        value = 0
                    logger.debug("Check port is ${value}")
                    state = "Check port is ${value}"
                } else {
                    logger.debug("No check port")
                    state = "No check port"
                }


                if (value == 1) {
                    m.message("Set port ", "cooldown")
                    state = " Set port Loop:${loop} run time :${i}"
                    setport(ports)
                    logger.debug("Set port is ok and wait ${runtime} cooldown")
                    state = "Set port is ok run ${runtime}"
                    TimeUnit.SECONDS.sleep(runtime!!)
                    resetport(ports)
                    state = "Reset port is ok wait time to end this Thread ${waittime}  loop ${i + 1} / ${loop} ${Date()}"
                    logger.debug("Set reset port is ok and wait ${waittime} cooldown  loop ${i + 1} / ${loop} ${Date()}")
                }
                state = "Start wait Check port ${value}  loop ${i + 1}/${loop} ${Date()}"
                logger.debug("Start wait Check port ${value}  loop ${i + 1}/${loop} ${Date()}")
                TimeUnit.SECONDS.sleep(waittime!!)
                logger.debug("End cool down job cooldown")
                state = "End job"
                m.message("End cool down job ", "cooldown")
                i++

            }
        } catch (e: Exception) {
            logger.error("" +
                    "counter WOrking :${e.message}")
        }






        isRun = false
    }

    //ใช้สำหรับ check port
    fun findcheckport(ports: List<Portstatusinjob>): ArrayList<Portstatusinjob> {
        var tochecks = ArrayList<Portstatusinjob>()
        for (port in ports) {
            var pn = port.portname?.name?.toLowerCase()
            if (pn.equals("check")) {
                tochecks.add(port)
            }
        }

        return tochecks
    }

    fun readDs(): BigDecimal? {

        try {
            var desid = pijob.desdevice_id
            var sensorid = pijob.ds18sensor_id

            var value = i.readDs18(pijob.ds18sensor?.name!!)
            if (value == null) {
                var dsvalue = ss.readDsOther(desid!!, sensorid!!)
                if (dsvalue != null) {
                    value = dsvalue.t
                }
            }

            return value
        } catch (e: Exception) {
            state = "Error : ReadDS() ${e.message}"
        }
        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CDWorker::class.java)
    }
}