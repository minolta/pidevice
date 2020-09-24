package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import me.pixka.kt.pibase.d.Pm
import me.pixka.kt.pibase.d.PmService
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.PideviceService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal
import java.util.*

@DataJpaTest
class TestaddPmandSent {
    @Autowired
    lateinit var http: HttpService

    var om = ObjectMapper()

    @Autowired
    lateinit var ps: PmService

    @Autowired
    lateinit var pds: PideviceService
    fun add() {
        var pm = Pm()
        pm.pm25 = BigDecimal(20)
        pm.pm1 = BigDecimal(11)
        pm.pm10 = BigDecimal(10.2)
        pm.valuedate = Date()
        var pd = PiDevice()
        pd.name = "DUST-2"
        pd.mac = "84:F3:EB:CC:CF:A0"
        pm.pidevice = pds.save(pd)
        ps.save(pm)
    }

    @Test
    fun AddAndSent() {
        add()

        var all = ps.all()


        all.forEach {

            var re = http.post("http://localhost:8080/pm/add", it)
            var pm = om.readValue<Pm>(re)
            println(pm)
        }
    }
}