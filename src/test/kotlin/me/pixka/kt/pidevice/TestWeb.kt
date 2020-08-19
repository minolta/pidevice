package me.pixka.kt.pidevice

import org.hamcrest.Matchers.containsString
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc


@SpringBootTest
@AutoConfigureMockMvc
class TestWeb ()
{
    @Autowired
    lateinit var  mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun shouldReturnDefaultMessage() {
        var re = mockMvc!!.perform(get("/pijob")).andDo(print()).andExpect(status().isOk()).andReturn()
        println(re)
//                .andExpect(content().string(containsString("Hello, World")))
    }
}