package me.pixka.kt.pidevice

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import me.pixka.kt.pibase.d.*
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pidevice.s.CheckTimeService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.worker.DisplaytmpWorker
import me.pixka.log.d.LogService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class Testdisplaydevice {

    fun readTmp() {
        var http = spyk<HttpService>()

    }

    @Test
    fun testSenddisplay() {
        var http = spyk<HttpService>()
        var url = URLEncoder.encode("ความร้อน : ทดสอบครับ  20001", StandardCharsets.UTF_8.toString());

        var re = http.get("http://192.168.89.234/settext?t=${url}&tn=3", 5000)
        http.get("http://192.168.89.234/settext?t=${url}&tn=2", 5000)

        println(re)
    }

    @Test
    fun testWorker() {

        var ct = spyk<CheckTimeService>()
        var dd = spyk<PiDevice>()
        dd.mac = "99:99:99:99"
        var d = spyk<PiDevice>() // for display
        d.name = "ทดสอบเตา 5000"
        d.mac = "00:00:00:00"


        var http = spyk<HttpService>()
        var task = mockk<TaskService>()

        var lgs = mockk<LogService>(relaxed = true)
        var pj = spyk<Pijob>()
        var ips = mockk<IptableServicekt>()


        every { ips.findByMac("99:99:99:99") } returns Iptableskt("5000", "192.168.89.23", "99:99:99:99", Date(), Date())
        every { ips.findByMac("00:00:00:00") } returns Iptableskt("5000", "192.168.89.238", "00:00:00:00", Date(), Date())
        var ed = Date()
        every { task.findExitdate(pj) } returns ed
        pj.name = "Test display"
        pj.runtime = 60
        pj.waittime = 120
        pj.desdevice = dd
        var pij = Portstatusinjob()
        pij.device = d
        var ports = listOf<Portstatusinjob>(pij)
        var w = DisplaytmpWorker(pj, lgs, http, ips, ports,ct)
        w.run()
        println("StartDATE: ${w.startRun} Exitdate:${w.exitdate}")
//        println(w.exitdate)
        findExitdate(pj)

//        Assertions.assertTrue(w.exitdate?.compareTo(ed) == 0)
    }

    fun findExitdate(pijob:Pijob)
    {
        var tvalue:Long? = 0L
        if (pijob.waittime != null)
            tvalue = pijob.waittime!!
        if (tvalue != null) {
            if (pijob.runtime != null)
                tvalue += pijob.runtime!!
        }
        val calendar = Calendar.getInstance() // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.SECOND, tvalue!!.toInt())
        val exitdate = calendar.time
        println("EXIT:"+exitdate)
    }
}