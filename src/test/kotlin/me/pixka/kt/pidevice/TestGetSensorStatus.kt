package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.spyk
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.run.DPortstatus
import org.junit.jupiter.api.Test

class TestGetSensorStatus {

    @Test
    fun testGetPortStatus() {
        val om = ObjectMapper()
        var httpService = spyk<HttpService>()
        val re: String? = httpService.get("http://192.168.89.235", 20000)
        println(re)
        val dp = om.readValue<DPortstatus>(re!!)
        println(dp.d5)
    }
}