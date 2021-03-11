package me.pixka.kt.pidevice.c

import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.s.PideviceService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class TestShowDevices {

    @Autowired
    private val mockMvc: MockMvc? = null

    @Autowired
    lateinit var  ps:PideviceService
    @Test
    fun p()
    {

        ps.findOrCreate("0000")
        mockMvc!!.perform(MockMvcRequestBuilders.get("/devices")).andDo(MockMvcResultHandlers.print()).andExpect(
            MockMvcResultMatchers.status().isOk)
    }
}