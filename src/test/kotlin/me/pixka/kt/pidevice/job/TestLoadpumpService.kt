package me.pixka.kt.pidevice.job

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import me.pixka.kt.pibase.d.Pumpforpijob
import me.pixka.kt.pibase.d.PumpforpijobService
import me.pixka.kt.pidevice.s.LoadpumpService
import me.pixka.kt.pidevice.s.MactoipService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestLoadpumpService {

val om = ObjectMapper()
    @Test
    fun testLoadpum() {//289

        var re = ArrayList<Pumpforpijob>()
        re.add(Pumpforpijob())
        var mtp = mockk<MactoipService>(relaxed = true)
        var ps = mockk<PumpforpijobService>(relaxed = true)
        var loadpumpService = LoadpumpService(mtp, ps)
        var url = "http://localhost:8080/pump/"
        every { mtp.http.get("http://localhost:8080/pump/264",60000) } returns om.writeValueAsString(re)
        var list = loadpumpService.loadPump(264,url)

        Assertions.assertTrue(list.size > 0)
    }


}