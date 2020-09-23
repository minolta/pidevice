package me.pixka.kt.run

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Profile("pi", "lite")
class CDWorker(var pj: Pijob, val ss: SensorService, var gi: GpioService,
               val m: MessageService, val i: Piio, val ppp: PortstatusinjobService,
               val pjs: PijobService) : Worker(pj, gi, i, ppp) {
    val d = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val dn = SimpleDateFormat("yyyy/MM/dd")
    override fun setrun(p: Boolean) {
        isRun = p
    }
    override fun run() {
        startrun = Date()
        try {
            isRun = true
            startrun = Date()
            var ports = ps.findByPijobid(pijob.id) as ArrayList<Portstatusinjob>
            var checkports = findcheckport(ports)


            logger.debug("cooldown Items is ${ports}  size ${ports.size} ")
            logger.debug("cooldown port size ${checkports.size} : ${ports}")


            var runtime = pijob.runtime
            var waittime = pijob.waittime
            jobid = pijob.id
            var loop = 1
            if (pijob.timetorun != null) {
                if (pijob.timetorun!!.toInt() > 0)
                    loop = pijob.timetorun!!.toInt()
            }
            logger.debug("cooldown CDWorker ${pijob.id}")
            m.message("Start Check High tmp", "cooldowninfo")
            this.state = "Start check High tmp"
            //  ms.message("Start work id:${pijob.id} ", "info")


            //เริ่มการหายอดสูงสุด
            while (true) {

                var value = readDs()
                if (value == null) {
                    logger.error("cooldown checkhigh Can not read DS18value in cooldown worker")
                    state = "Can not read high tmp"
                    TimeUnit.SECONDS.sleep(60)
                    continue
                }
                logger.debug("cooldown checkhigh High tmp check ${value}")
                if (value.toInt() > pijob.thigh!!.toInt()) {
                    //รอสองนาที
                    m.message("cooldown checkhigh High tmp is over and wait 120 sec", "cooldownrun")
                    logger.debug("cooldown checkhigh  High tmp is ok and wait 120 sec to check again")
                    state = "cooldown checkhigh  Hi tmp is ok check again"
                    TimeUnit.SECONDS.sleep(120)

                    value = readDs()
                    if (value == null) {
                        logger.error("cooldown checkhigh  Can not read DS18value in cooldown worker")
                        state = "cooldown checkhigh  Can not read high tmp"
                        TimeUnit.SECONDS.sleep(60)
                        continue
                    }
                    if (value.toInt() > pijob.thigh!!.toInt()) {
                        m.message("cooldown checkhigh  Check High tmp is ok have to wait low tmp", "cooldownrun")
                        logger.debug("cooldown checkhigh  High tmp is ok next check lowtmp")
                        state = "cooldown checkhigh Tmp is ok go to check low tmp"
                        break //ถ้าความร้อนถึงแล้วเข้าสู่ mode รอให้ ความร้อนลงตำสุดก่อน
                    } else {
                        logger.error("cooldown checkhigh High tmp not ready to next")
                    }
                }

                logger.error(" cooldown checkhigh  ${value} < ${pijob.thigh} in cooldown worker")
                state = "still high tmp wait to check again Date ${Date()}"
                TimeUnit.SECONDS.sleep(10)
            }

            state = " start check low tmp"
            logger.debug("cooldown checklow  High tmp is ok next check lowtmp")
            //รอจนกว่า ความร้อนจะลงตำสุดระบบจะเริ่มทำงาน
            while (true) {
                var value = readDs()
                if (value == null) {
                    m.message("cooldown checklow Can not read Low tmp", "cooldownrun")
                    logger.error(" cooldown checklow Canon read low tmp")
                    state = " cooldown checklow can not read low tmp"
                    TimeUnit.SECONDS.sleep(120)
                    continue
                }
                logger.debug("  cooldown checklow Low tmp check ${value}")
                state = " low tmp is ${value}"
                if (value.toInt() < pijob.tlow!!.toInt()) {
                    //รอสองนาที
                    m.message(" cooldown checklow Low tmp is under and wait 120 sec", "cooldownrun")
                    state = "Low tmp is under and wait 120 sec"
                    logger.debug("  cooldown checklow Low tmp check ${value} wait 120 sec")
                    TimeUnit.SECONDS.sleep(120)

                    value = readDs()
                    if (value!!.toInt() < pijob.tlow!!.toInt()) {
                        m.message("Check Low tmp is ok have to run this job ${pijob}", "cooldownrun")
                        state = "Now low tmp is ok go to run jobs"
                        logger.debug(" cooldown checklow low this ok next to check date")
                        break //ถ้าความร้อนถึงแล้วเข้าสู่ mode รอให้ ความร้อนลงตำสุดก่อน
                    } else {
                        logger.error(" cooldown checklow  Low  tmp not ready to run cooldown")
                        state = "low tmp not ready to run"
                    }
                }


                logger.error("  cooldown checklow  ${value} > ${pijob.tlow} in cooldown worker")
                state = "Low tmp is ${value} > ${pijob.tlow}"
                TimeUnit.SECONDS.sleep(10)

            }

            checktime()


            var i = 0

            logger.debug(" cooldown Check port cooldown loopis ${loop}")

            while (i < loop) {

                var value = 1
                if (checkports.size > 0) {

                    var port = checkports[0].portname
                    var p = gpio.readPort(port?.name!!)
                    if (p!!.isHigh) {
                        value = 1
                    } else
                        value = 0
                    logger.debug("cooldown checkport Check port is ${value}")
                    state = "Check port is ${value}"
                } else {
                    logger.debug(" cooldown checkport No check port")
                    state = "No check port"
                }


                if (value == 1) {
                    m.message("Set port ", "cooldown")
                    state = " Set port Loop:${loop} run time :${i}"
                    setport(ports)
                    logger.debug(" cooldown checkport Set port is ok and wait ${runtime} cooldown")
                    state = "Set port is ok run ${runtime}"
                    TimeUnit.SECONDS.sleep(runtime!!)
                    resetport(ports)
                    state = "Reset port is ok wait time to end this Thread ${waittime}  loop ${i + 1} / ${loop} ${Date()}"
                    logger.debug(" cooldown checkport  Set reset port is ok and wait ${waittime} cooldown  loop ${i + 1} / ${loop} ${Date()}")
                }
                state = "Start wait Check port ${value}  loop ${i + 1}/${loop} ${Date()}"
                logger.debug(" cooldown checkport  Start wait Check port ${value}  loop ${i + 1}/${loop} ${Date()}")
                TimeUnit.SECONDS.sleep(waittime!!)
                logger.debug(" cooldown checkport End cool down job cooldown")
                state = "End job"
                m.message("End cool down job ", "cooldown")
                i++

            }
        } catch (e: Exception) {
            logger.error("cooldown WOrking :${e.message}")
        }






        isRun = false
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
            logger.error("cooldown getnextrun pares date ${e.message}")
        }
        return null
    }

    fun checktime() {
        if (pijob.stimes != null) {
            state = "Have stime this job have to start after ${pijob.stimes}"
            logger.debug("cooldown checktime Check time wait time : ${pijob.stimes}")

            var timetorun = getnextrunt()

            while (true) {
                var now = Date().time
                state = "Next run ${timetorun?.time} now ${now}"
                logger.debug("cooldown checktime checktime nextrun time wait time :  ${timetorun!!.time} now ${now}")
                if (now.toInt() >= timetorun?.time?.toInt()!!) {
                    state = "exit time check  Start run now"
                    logger.debug("cooldown checktime checktime Start to run job now")
                    break
                }

                state = "Not have time to run"
                logger.debug("cooldown checktime  wait time ")
                TimeUnit.SECONDS.sleep(10)
            }
        }
    }


    fun readDs(): BigDecimal? {

        try {
            var desid = pijob.desdevice_id
            var sensorid = pijob.ds18sensor_id

//            var value = i.readDs18(pijob.ds18sensor?.name!!)
            var value = i.readDs18()
            if (value == null) {
                var dsvalue = ss.readDsOther(desid!!, sensorid!!)
                if (dsvalue != null) {
                    value = dsvalue.t!!
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