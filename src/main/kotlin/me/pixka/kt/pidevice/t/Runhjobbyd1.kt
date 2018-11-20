package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.base.s.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PideviceService
import me.pixka.pibase.s.PijobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Runhjobbyd1(val http: HttpControl,
                  val pjs: PijobService,
                  val js: JobService,
                  val dhts: DhtvalueService,
                  val pds: PideviceService,
                  val ips: IptableServicekt) {
    val om = ObjectMapper()

    @Scheduled(fixedDelay = 5000)
    fun run() {

    }

    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runhbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }
}