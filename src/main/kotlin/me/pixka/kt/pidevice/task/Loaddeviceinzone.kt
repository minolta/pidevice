package me.pixka.kt.pidevice.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Deviceinzone
import me.pixka.kt.pibase.d.DeviceinzoneService
import me.pixka.kt.pibase.d.PijobgroupService
import me.pixka.kt.pibase.s.PideviceService
import me.pixka.kt.pidevice.s.MactoipService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Loaddiz(
    val mtp: MactoipService, val ds: PideviceService, val pgs: PijobgroupService,
    val dizs: DeviceinzoneService
) {

    val om = ObjectMapper()

    @Scheduled(fixedDelay = 2000)
    fun load() {
        try {
            var target = System.getProperty("piserver")
            var re = mtp.http.get(target + "/deviceinzone/list")
            var list = om.readValue<List<Deviceinzone>>(re)
            save(list)
        } catch (e: Exception) {
            logger.error("Load Device in zone ERROR ${e.message}")

        }

    }

    fun save(list: List<Deviceinzone>) {
        try {
            list.forEach {

                var d = ds.findOrCreate(it.pidevice?.mac!!)
                var g = pgs.findOrCreate(it.pijobgroup?.name!!)

                var found = dizs.find(d.id, g!!.id)
                if (found == null) {
                    var f = Deviceinzone()
                    f.pidevice = d
                    f.pijobgroup = g
                    dizs.save(f)
                }

            }
        }catch (e:Exception)
        {
            logger.error("ERROR ${e.message}")
        }
    }

    var logger = LoggerFactory.getLogger(Loaddiz::class.java)
}