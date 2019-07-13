package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Onecommand
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.kt.pidevice.s.NotifyService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.ReadUtil
import me.pixka.kt.run.CountdownWorkerii
import me.pixka.kt.run.HWorker
import me.pixka.kt.run.PijobrunInterface
import me.pixka.kt.run.Worker
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException


@Component
@Profile("pi", "lite")
class Runonecommand(val dhts: DhtvalueService, val ts: TaskService, val notifyService: NotifyService,
                    val pjs: PijobService, val js: JobService,
                    val gpios: GpioService, val ss: SensorService,
                    val m: MessageService, val i: Piio, val readUtil: ReadUtil,
                    val ms: MessageService, val io: Piio, val http: HttpControl, val ps: PortstatusinjobService) {


    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    fun run() {

        var ones = loadCommand(io.wifiMacAddress())

        if (ones != null)
            for (o in ones) {


                var pijob = pjs.findByRefid(o.pijob_id)

                var type = pijob?.job?.name

                if (type.equals("DS")) {
                    var worker = Worker(pijob!!, gpios, io, ps)
                    logger.debug("Run  ${pijob} in onecommand")
                    notifyService.message("Run onecommand DS job ${pijob.name}")
                    ts.run(worker)
                    if (pijob.runwithid != null) {
                        var runwith = pjs.findByRefid(pijob.runwithid)
                        var w = Worker(runwith!!, gpios, io, ps)
                        ts.run(w)
                        logger.debug("Run With ==> ${w} in onecommand")
                    }
                } else if (type.equals("DSOTHER")) {

                } else if (type.equals("cooldown")) {
                    var w = CountdownWorkerii(pijob!!, gpios, ss, notifyService, readUtil)

                    var r = ts.run(w)
                    if (r) {
                        logger.debug("Run command cooldown ${w}")
                    } else
                        logger.error("Not run cooldown ${w}")

                    notifyService.message("Run onecommand Cooldown job ${pijob.name} is run ${r}")
                }

            }

    }

    fun selectWorker(o: Onecommand): PijobrunInterface? {
        var pijob = pjs.findByRefid(o.pijob_id)
        var jobname = pijob?.job?.name
        if (jobname?.indexOf("DS", 0) != null) {
            return Worker(pijob!!, gpios, io, ps)
        }

        return null
    }

    fun loadCommand(mac: String): List<Onecommand>? {
        var target = System.getProperty("piserver")
        try {
            val mapper = ObjectMapper()
            var url = target + "/one/get/" + mac
            logger.debug("Load one command  at ${url}")
            val re = http.get(url)
            val list = mapper.readValue<List<Onecommand>>(re)
            logger.debug("[onecommand] Found commands for me " + list.size)
            return list
        } catch (e: IOException) {
            logger.error("Load command error ${e.message}")

        }
        return null
    }

    fun exec(jobs: List<Pijob>) {

        for (j in jobs) {
            var work = HWorker(j, gpios, ms, io, ps) //เปลียนเป็น hwork
            ts.run(work)
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runonecommand::class.java)
    }
}