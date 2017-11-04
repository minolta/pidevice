package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.run.DSOTHERWorker
import me.pixka.pibase.d.DS18value
import me.pixka.pibase.d.Pijob
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("pi")
class Finddsjobbyother(var pjs: PijobService, var js: JobService,
                       val ts: TaskService,
                       val gpios: GpioService,
                       val ss: SensorService) {
    private val om = ObjectMapper()
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    fun find() {
        logger.debug("Start run DSOTHER")
        var DSOTHER = js.findByName("DSOTHER")

        if (DSOTHER == null) {
            logger.error("NOT Found job DSOTHER ")
            return
        }

        //ค้นหางานที่ต้องอ่านค่า DS จากตัวอื่น
        var jobs = pjs.findDSOTHERJob(DSOTHER.id)
        var jobforrun = ArrayList<Pijob>()
        if (jobs != null) {
            for (job in jobs) {
                //ถ้ามี job ที่ต้องอ่านจากตัวอื่น

                var j = findCanrunjob(job)

                if (j != null)
                    jobforrun.add(j)
            }
        }

        logger.debug("DS OTHER jobs for run ${jobforrun.size}")
        //หลังจากได้ job ที่อยู่ในเงือนไขที่จะทำงานแล้วก็ส่งไปทำงาน
        if (jobforrun.size > 0)
            for (r in jobforrun) {
                var w = DSOTHERWorker(r, gpios)
                ts.run(w)
            }

        // pjs.findByDSOrther()


    }

    fun findCanrunjob(job: Pijob): Pijob? {

        var desid = job.desdevice_id
        var sensorid = job.ds18sensor_id


        if (desid != null) {//ถ้าระบุ desid ให้ทำงานได้

            var dsvalue: DS18value? = null
            if (sensorid != null) {//ถ้าระบุ sensor id ด้วย ให้ อ่าน จาก sensor ด้วย
                dsvalue = ss.readDsOther(desid, sensorid)
            } else {
                //ถ้าไม่ระบุให้อ่านจาก Default sensor
                dsvalue = readDsOther(desid)
            }


            if (dsvalue != null) {
                var tlow = job.tlow
                var thigh = job.thigh
                var v = dsvalue.t

                if (v?.compareTo(tlow)!! >= 0 && v.compareTo(thigh) <= 0) {
                    return job
                }
            }


        }

        return null
    }


    fun readDsOther(otherid: Long): DS18value? {

        return null
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Finddsjobbyother::class.java)
    }

}