package me.pixka.kt.pidevice.c

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


@RestController
@Profile("pi", "lite")
class Exportpijob(val ps: PijobService, val piio: Piio, val psijs: PortstatusinjobService) {
    @CrossOrigin
    @RequestMapping(value = ["/pijob"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun value(): List<Pijob> {
        var all = ps.all() as List<Pijob>
        return addinfo(all)
    }

    fun addinfo(all: List<Pijob>): ArrayList<Pijob> {
        var buf = ArrayList<Pijob>()
        for (item in all) {
            var p = PiDevice()
            p.mac = piio.wifiMacAddress()
            item.pidevice = p
            var portlist = psijs.findByPijobid(item.id) as List<Portstatusinjob>
            logger.debug("${portlist}")
            item.ports = portlist
            buf.add(item)
        }
        return buf
    }

    @RequestMapping(value = ["/exportjob"], method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @CrossOrigin
    fun exportjobs(): ResponseEntity<InputStreamResource>? {


        try {
            val objectMapper = ObjectMapper()

            var all = ps.all() as List<Pijob>
            var buf = addinfo(all)

            var file = File.createTempFile("export", "xxxx")
            var fout = FileOutputStream(file)
            objectMapper.writeValue(fout, buf)
            fout.close()

            var fileinput = FileInputStream(file)

            val respHeaders = HttpHeaders()
            respHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
            respHeaders.setContentLength(file.length())
            respHeaders.setContentDispositionFormData("attachment", "exports.json")

            val isr = InputStreamResource(fileinput)
            return ResponseEntity(isr, respHeaders, HttpStatus.OK)

        } catch (e: Exception) {
            logger.error("${e.message}")
        }
        return null
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Exportpijob::class.java)
    }
}