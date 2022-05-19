package me.pixka.kt.pidevice.d

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.Sensorinjob
import me.pixka.kt.pibase.d.SensorinjobService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class TestloadSensorinjob {

    @Autowired
    lateinit var ss: SensorinjobService

    val om = ObjectMapper()
    @Test
    fun testload() {
        var s = "[{\"sensor\":{\"id\":1,\"name\":\"s1\"},\"pijob\":{\"id\":1,\"name\":\"p1\"}   }]"


        var list = om.readValue<List<Sensorinjob>>(s)


        println(list)

    }
}