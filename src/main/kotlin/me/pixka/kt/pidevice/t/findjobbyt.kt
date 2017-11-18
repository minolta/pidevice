package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.o.DS18obj
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.Worker
import me.pixka.pibase.d.DS18sensor
import me.pixka.pibase.d.DS18value
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.Ds18valueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
ใช้สำหรับ ค้นหา job ที่มา run สำหรับ DS18b20
จะต้องอ่านค่า DS18B20 แล้วทำการหา ข้อมูล job ที่ตรงออกมา
ทุก sensor ใน device
 **/

@Component
@Profile("pi")
class FindJobforRunDS18value(val dsvs: Ds18valueService, val dss: DS18sensorService,
                             val pjs: PijobService, val js: JobService, val gpios: GpioService,
                             val ts: TaskService,var dsobj:DS18obj) {

    @Scheduled(initialDelay = 5000,fixedDelay = 10000)
    fun run() {
        logger.info("Run Find JOB for run ds18b20")
        var DSJOB = js.findByName("DS")
        var lasts = dsobj.findlast()

        logger.debug("Found last ${lasts}")
        if (lasts != null) {
            var buf = ArrayList<Pijob>()
            for (last in lasts) {
                logger.info("Job: ${last}")
                var t = last.ds18value?.t
                var sensorid = last.dssensor?.id
                var dsjobid = DSJOB.id
                if (t != null && sensorid != null && dsjobid != null) {

                    var jobs = pjs.findDSJOBBySensor(t, sensorid, dsjobid)
                    logger.debug("Job for sensor ${last.dssensor?.name}  ${jobs}")
                    if (jobs != null) {
                        buf.addAll(jobs)//เพิ่มเข้าที่จะต้อง run
                        logger.debug("Add pijob to check size: ${buf.size} ${buf}")
                    }
                }
            }
            //ถ้ามี ่ job ก็ทำการ run หรือส่งไปยัง Runpijob
            logger.debug("Jobs ${buf.size}")
            if (buf.size > 0) {
                runpiJob(buf)
            }
        }

        logger.debug("End Run")
    }

    fun runpiJob(runs: ArrayList<Pijob>) {
        for (r in runs) {
            var w = Worker(r, gpios)
            ts.run(w)
        }
    }





    companion object {
        internal var logger = LoggerFactory.getLogger(FindJobforRunDS18value::class.java)
    }
}

class Dssensorforfindlast(var dssensor: DS18sensor? = null, var ds18value: DS18value? = null) {
    override fun toString(): String {
        return "${dssensor?.name}  ${ds18value?.t}"
    }
}
