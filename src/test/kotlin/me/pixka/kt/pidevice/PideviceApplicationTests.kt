package me.pixka.kt.pidevice

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.run.D1portjobWorker
import me.pixka.kt.run.DPortstatus
import me.pixka.kt.run.PorttoCheck
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class PideviceApplicationTests {
    @Autowired
    lateinit var http: HttpControl

    @Test
    fun contextLoads() {
      test1()
    }

    fun testgetStatus()
    {
        val mapper = ObjectMapper()
        val re = http.get("http://192.168.88.160")
//        val list = mapper.readValue<List<DPortstatus>>(re)
//        val dp = mapper.readValue(re,DPortstatus)
//        val re = "{\"d1\":2,\"d2\":3}"
        var dp = mapper.readValue(re, DPortstatus::class.java)
        println(dp)
    }
    fun test2() {
        var bufs = ArrayList<PorttoCheck>()

        var c = "D5,1".split(",")
        D1portjobWorker.logger.debug("C: ${c}")
        if (c.isNullOrEmpty()) {
            println("ERROR")
        }

    }

    fun test1() {
        var description = "D5,1,D6,1"
        var bufs = ArrayList<PorttoCheck>()
        var c = description?.split(",")
        if (c == null || c.size < 1) {
            println("False")
        }

        var c1 = PorttoCheck()
        c.map {
            D1portjobWorker.logger.debug("Value : ${it}")
            if(it.toIntOrNull()==null)
                c1.name = it
            else {
                c1.check = it.toInt()
                bufs.add(c1)
             //   c1 = PorttoCheck()
            }
        }
        var ps = DPortstatus()
        ps.d5=1
        ps.d6=1
        getsensorstatusvalue(c1.name?.toLowerCase()!!,c1.check!!,ps)
        println(bufs)
    }

    fun getsensorstatusvalue(n: String, c: Int, sensorstatus: DPortstatus?): Boolean {
        val nn = n.toLowerCase()
        try {
            if (nn.equals("d1")) {
                if (sensorstatus?.d1 == c)
                    return true
            } else if (nn.equals("d2")) {
                if (sensorstatus?.d2 == c)
                    return true
            } else if (nn.equals("d3")) {
                if (sensorstatus?.d3 == c)
                    return true
            } else if (nn.equals("d4")) {
                if (sensorstatus?.d4 == c)
                    return true
            } else if (nn.equals("d5")) {
                if (sensorstatus?.d5 == c)
                    return true
            } else if (nn.equals("d6")) {
                if (sensorstatus?.d6 == c)
                    return true
            } else if (nn.equals("d7")) {
                if (sensorstatus?.d7 == c)
                    return true
            } else if (nn.equals("d8")) {
                if (sensorstatus?.d8 == c)
                    return true
            }
        } catch (e: Exception) {
            D1portjobWorker.logger.error("ERROR in getsensorstatusvalue message: ${e.message}")
        }


        return false
    }

}
