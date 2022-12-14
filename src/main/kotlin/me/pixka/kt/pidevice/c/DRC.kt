package me.pixka.kt.pidevice.c

import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.WaterflowService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.d.ConfigdataService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.WarterLowPressureService
import me.pixka.kt.run.D1hjobWorker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class DRC(val ps: PijobService,val mtp:MactoipService,val ntf:NotifyService,val ts:TaskService,val lps:WarterLowPressureService,val cs:ConfigdataService) {


    @GetMapping(path = ["/rundirect/{id}"])
    fun directrun(@PathVariable("id") refid: Long): Boolean {
        try {
            var  pijob = ps.findByRefid(refid)

            if(pijob!=null)
            {
                if(pijob.job?.name.equals("runhbyd1"))
                {
                    var task = D1hjobWorker(pijob,mtp,ntf,lps,cs)
                    return ts.run(task)
                }

                throw Exception("Not support job ${pijob}")
            }

            throw Exception("job not found : ${refid}")
        }
        catch (e:Exception)
        {
            throw e
        }
    }
}