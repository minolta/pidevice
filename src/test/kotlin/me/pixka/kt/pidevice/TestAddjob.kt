package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc


@SpringBootTest
@AutoConfigureMockMvc
class TestAddjob ()
{
    @Autowired
    lateinit var  mockMvc: MockMvc

    @Autowired
    lateinit  var objectMapper: ObjectMapper

    @Test
    fun test()
    {
//        var bank = Bank("11111")
//        var b = objectMapper.writeValueAsString(bank)
//        mockMvc.perform(post("/bank/add/")
//                .contentType(MediaType.APPLICATION_JSON).body(b).).andReturn()
////                .content(objectMapper.writeValueAsString(user)))
////                .andExpect(status().isCreated());
    }
}