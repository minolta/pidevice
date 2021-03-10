package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.D1TWorkerII
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
@Profile("!test")
class RunTII(
    val findJob: FindJob, val checkTimeService: CheckTimeService, val taskService: TaskService,
    val readTmpService: ReadTmpService, val mtp: MactoipService
) {

    var buffer = ArrayList<Pijob>()

    @Scheduled(fixedDelay = 1000)
    fun getToRun() {
        var jobs = findJob.loadjob("runtbyd1")

        if (jobs != null) {
            jobs.forEach {
                var id = it.id

                //ตรวจสอบว่ามีการทำงานอยู่เปล่า
                try {
                    if (buffer.find { it.id == id } == null) {

                        if (checkTimeService.checkTime(it, Date()) && !taskService.checkrun(it)) {
                            try {
                                buffer.add(it) //เพิ่มเข้าไปว่า run แล้ว
                                if (readTmp(it) && !taskService.checkrun(it)) {
                                    var t = D1TWorkerII(it, mtp, readTmpService)
                                    if (taskService.run(t)) {
                                        buffer.remove(it)
                                    }
                                }
                            } catch (e: Exception) {
                                logger.error("ERROR ${e.message} ${it.name}")
                            }
                        }
                    } else {
                        //ยัง run ไม่จบไม่ run
                    }
                } catch (e: Exception) {
                    logger.error("${it.name} ${e.message}")
                }
            }
        }

    }

    fun readTmp(pijob: Pijob): Boolean {
        try {
            var t: BigDecimal? = null
            try {
                t = mtp.readTmp(pijob, 10000)
            } catch (e: Exception) {
                logger.error("${pijob.name} Read Tmp ERROR ${e.message}")
                throw e
            }
            var tl = pijob.tlow!!.toDouble()
            var th = pijob.thigh!!.toDouble()
            var value = t?.toDouble()
            if (tl <= value!! && value <= th)
                return true
            return false
        } catch (e: Exception) {
            logger.error("${pijob.name} Read Tmp: ERROR ${e.message} ${pijob.name}  ${pijob.desdevice?.name}")
            mtp.lgs.createERROR(
                "${e.message} ${pijob.name}  ${pijob.desdevice?.name}", Date(),
                "RUNTII", Thread.currentThread().name, "54", "readTmp()",
                pijob.desdevice?.mac, pijob.refid
            )
            throw e
        }
    }

    val logger = LoggerFactory.getLogger(RunTII::class.java)
}