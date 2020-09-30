package me.pixka.kt.pidevice

import me.pixka.kt.pibase.d.Dhtvalue
import me.pixka.kt.pibase.s.DhtvalueService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal

@DataJpaTest
class TestSaveDhtvalue()
{

    @Autowired
    lateinit var dhts:DhtvalueService


    @Test
    fun testSave()
    {
        var dhtvalue = Dhtvalue()
        dhtvalue.h = BigDecimal(99.29)
        dhtvalue.t = BigDecimal(20.2)

        var dht = dhts.save(dhtvalue)
        Assertions.assertTrue(dht.t?.toDouble()==20.2)
    }
}