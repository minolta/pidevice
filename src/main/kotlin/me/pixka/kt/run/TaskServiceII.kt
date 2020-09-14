package me.pixka.kt.run

import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * สำหรับ run Task แบบใหม่
 */
@Service
class TaskServiceII {
    var runinglist = ArrayList<PijobrunInterface>() // สำหรับบอกว่าตัวไหนจะ ยัง run อยู่
//    val threadpool = ThreadPoolExecutor(10, 500, 1,
//            TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>(1000),
//            ThreadPoolExecutor.CallerRunsPolicy())

    var threadpool = Executors.newFixedThreadPool(16)
//    fun activeCount(): Int {
//        return threadpool.activeCount
//    }

    fun activeCount():Int{
        return 0
    }

    fun checkrun(work: PijobrunInterface): Boolean {
        var found = runinglist.find { it.getPijobid() == work.getPijobid() }
        if (found != null)
            return found.runStatus()


        return false
    }

    fun removeEndjob(): List<PijobrunInterface> {

        var run =  runinglist.filter { it.runStatus()==false }
        this.runinglist.removeAll(run)
        return runinglist
    }

    fun active(): Int {
        var a = 0
        runinglist.map {

            if (it.runStatus()) {
                a++
            }
        }
        return a
    }

    fun run(work: PijobrunInterface): Boolean {
        if (!checkrun(work)) {
            var f = threadpool.submit(work as Runnable)
            runinglist.add(work)
            return true
        }
        return false
    }


}