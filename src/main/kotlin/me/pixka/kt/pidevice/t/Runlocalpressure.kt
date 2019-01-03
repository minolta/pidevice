package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pidevice.s.InfoService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.Worker
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class Runlocalpressure(val infoService: InfoService, val js: JobService, val pjs: PijobService,
                       val taskService: TaskService, val gpios: GpioService, val io: Piio,
                       val ps: PortstatusinjobService, val readUtil: ReadUtil) {

    @Scheduled(fixedDelay = 5000)
    fun run() {
        logger.debug("Load run loacal pressure Value ")
        var jobs = loadjob()
        logger.debug("Found pressure job ${jobs}")

        if (jobs != null) {
            for (job in jobs) {
                var j = check(job)
                if (j != null) {
                    var t = Worker(j, gpios, io, ps)
                    var canrun = taskService.run(t)
                    logger.debug("${j} is run ${canrun}")
                }
            }
        }

    }
 /*
 *
 * ตรวจสอบว่าแรงดันอยู่ในช่วงต้องช่วยหรือเปล่า
 * เอาแต่ที่ตำกว่า tl
 *
 * */

    fun check(pj: Pijob): Pijob? {
        var tl = pj.hlow?.toDouble()
        var th = pj.hhigh?.toDouble()
        var now = infoService.A0?.psi?.toDouble()
        logger.debug("===== ${tl} < ${now} > ${th} =====")
        try {
            checkReadTmp(pj)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
        //ทดสอบว่าแรงดันอยู่ใย่ช่วงหรือเปล่า
        if (tl!! >= now !! && now > 0) {
            logger.debug("Run this job")
            return pj
        }
        logger.error("Pressuer to high")
        return null


    }

    /**
     * ใช้สำหรับ ความร้อนช่วงกำหนดไว้หรือเปล่า
     */
    fun checkReadTmp(pj: Pijob): Pijob? {
        try {
            if (pj.desdevice != null) {
                var tl = pj.tlow?.toDouble()
                var th = pj.thigh?.toDouble()
                var tmp = readUtil.readTmpByjob(pj)
                logger.debug("Now check tmp ${tmp}")
                if (tl!! <= tmp!!.toDouble() && tmp.toDouble() <= th!!) {
                    return pj
                }
                throw Exception("Tmp not in job")

            }
        } catch (e: Exception) {

            logger.error(e.message)
            throw e
        }
        return null

    }


    fun loadjob(): List<Pijob>? {
        try {
            var job = js.findByName("runlocalpressure")
            if (job != null) {
                var jobtorun = pjs.findJob(job.id)
                return jobtorun
            }
            return null
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Runlocalpressure::class.java)
    }
}