package me.pixka.kt.pidevice.t

import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.ReadTmpService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.D1TWorkerII
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture

@Component
class RunTII(val findJob: FindJob, val checkTimeService: CheckTimeService, val taskService: TaskService,
             val readTmpService: ReadTmpService, val mtp: MactoipService) {

    @Scheduled(fixedDelay = 500)
    fun getToRun() {
        var jobs = findJob.loadjob("runtbyd1")

        if (jobs != null) {
            jobs.forEach {
                if (checkTimeService.checkTime(it, Date()) && !taskService.checkrun(it)) {
                    CompletableFuture.supplyAsync {
                        if (readTmp(it)) {
                            var t = D1TWorkerII(it, mtp, readTmpService)
                            taskService.run(t)
                        }

                    }.exceptionally {
                        logger.error(it.message)
                        null
                    }


                }
            }
        }

    }

    fun readTmp(pijob: Pijob): Boolean {
        try {

            var t = mtp.readTmp(pijob)
            var tl = pijob.tlow!!.toDouble()
            var th = pijob.thigh!!.toDouble()
            var value = t?.toDouble()
            if (tl <= value!! && value <= th)
                return true
            return false
        } catch (e: Exception) {
            logger.error("ERROR ${e.message} ${pijob.name}  ${pijob.desdevice?.name}")
            mtp.lgs.createERROR("${e.message} ${pijob.name}  ${pijob.desdevice?.name}", Date(),
                    "D1TWorkerII", Thread.currentThread().name, "59", "readTmp()",
                    pijob.desdevice?.mac, pijob.refid)
            throw e
        }
    }

    val logger = LoggerFactory.getLogger(RunTII::class.java)
}