package me.pixka.kt.pidevice.TaskService

import io.mockk.mockk
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.PijobrunInterface
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList

class TestCheckrun {

    var runinglist = CopyOnWriteArrayList<PijobrunInterface>()
    fun c(w:PijobrunInterface): Boolean {
            var items = runinglist.iterator()

            while (items.hasNext()) {
                var item = items.next()
                if (item.getPijobid() == w.getPijobid() && item.runStatus()) {
                    return true
                }
            }
            return false

    }
    @Test
    fun testCheckrun()
    {
       var p = Pijob()
        p.id=1
        var mtp = mockk<MactoipService>(relaxed = true)
        var ntf  =mockk<NotifyService>(relaxed = true)
        var run =D1hjobWorker(p,mtp,ntf)

        runinglist.add(run)

        p = Pijob()
        p.id = 2
        run = D1hjobWorker(p,mtp,ntf)
        run.isRun=true
        runinglist.add(run)


        Assertions.assertTrue(c(run))
    }
}