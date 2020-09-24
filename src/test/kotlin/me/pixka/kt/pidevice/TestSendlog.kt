package me.pixka.kt.pidevice

import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.s.HttpService
import me.pixka.log.d.LogService
import me.pixka.log.d.Logsevent
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.*

@DataJpaTest
class TestSendlog
{
    @Autowired
    lateinit var http:HttpService
    @Autowired
    lateinit var lgs:LogService

    @Test
    fun testSendLog()
    {
//        lgs.createERROR("Test ERROR", Date(),"Test class","",
//        "","","c8:3a:35:c3:74:f4")

        lgs.createERROR("Test ERROR", Date(),"Test class","",
                "","",null)

        var all = lgs.all()

        all.forEach {
            try {
                http.post("http://localhost:8080/addlog", it)
            }catch (e:Exception)
            {
                println(e)
            }
        }
    }
}