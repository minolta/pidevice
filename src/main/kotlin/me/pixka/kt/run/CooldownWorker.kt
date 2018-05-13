package me.pixka.kt.run

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit


@Profile("pi", "lite")
class CDWorker(var pj: Pijob, val ss: SensorService, var gi: GpioService,
               val m: MessageService, val i: Piio, val ppp: PortstatusinjobService) : Worker(pj, gi, i, ppp) {
    override fun run() {
        try {
            isRun = true
            startrun = Date()
            var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
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
            //  ms.message("Start work id:${pijob.id} ", "info")


            //เริ่มการหายอดสูงสุด
            while (true) {
                var value = readDs()

                if (value == null) {
                    logger.error("Can not read DS18value in cooldown worker")
                    TimeUnit.SECONDS.sleep(10000)
                    continue;
                }
                logger.debug("High tmp check ${value}")
                if (value!!.toInt() > pijob.thigh!!.toInt()) {
                    //รอสองนาที
                    m.message("High tmp is over and wait 120 sec", "cooldownrun")
                    logger.debug("High tmp is ok and wait 120 sec to check again")
                    TimeUnit.SECONDS.sleep(120)

                    value = readDs()
                    if (value!!.toInt() > pijob.thigh!!.toInt()) {
                        m.message("Check High tmp is ok have to wait low tmp", "cooldownrun")
                        logger.debug("High tmp is ok next check lowtmp")
                        break //ถ้าความร้อนถึงแล้วเข้าสู่ mode รอให้ ความร้อนลงตำสุดก่อน
                    }
                    else
                    {
                            logger.error("High tmp not ready to next")
                    }
                }

                logger.error(" DS18value ${value} < ${pijob?.thigh} in cooldown worker")
                TimeUnit.SECONDS.sleep(10000)
            }


            //รอจนกว่า ความร้อนจะลงตำสุดระบบจะเริ่มทำงาน
            while (true) {
                var value = readDs()
                if (value == null) {
                    m.message("Can not read Low tmp", "cooldownrun")
                    continue
                }
                logger.debug("Low tmp check ${value}")
                if (value!!.toInt()<  pijob.tlow!!.toInt()) {
                    //รอสองนาที
                    m.message("Low tmp is under and wait 120 sec", "cooldownrun")
                    TimeUnit.SECONDS.sleep(120)

                    value = readDs()
                    if (value!!.toInt() < pijob.tlow!!.toInt()) {
                        m.message("Check Low tmp is ok have to run this job ${pijob}", "cooldownrun")
                        break //ถ้าความร้อนถึงแล้วเข้าสู่ mode รอให้ ความร้อนลงตำสุดก่อน
                    }
                    else
                    {
                        logger.error("Low  tmp not ready to run cooldown")
                    }
                }


                logger.error(" DS18value ${value} > ${pijob?.tlow} in cooldown worker")
                TimeUnit.SECONDS.sleep(10000)

            }


            var i = 0

            if(pijob.timetorun!=null)
            {
                loop = pijob.timetorun?.toInt()!!
            }

            while(i < loop) {
                m.message("Set port ", "cooldown")
                setport(ports)
                logger.debug("Set port is ok and wait ${runtime} cooldown")
                TimeUnit.SECONDS.sleep(runtime!!)
                resetport(ports)
                logger.debug("Set reset port is ok and wait ${waittime} cooldown ")
                TimeUnit.SECONDS.sleep(waittime!!)

                logger.debug("End cool down job")
                m.message("End cool down job ", "cooldown")
                i++

            }
        } catch (e: Exception) {
            logger.error("" +
                    "counter WOrking :${e.message}")
        }






        isRun = false
    }

    fun readDs(): BigDecimal? {

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
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(CDWorker::class.java)
    }
}