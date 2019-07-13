package me.pixka.kt.pidevice.s

import me.pixka.kt.run.PijobrunInterface
import org.springframework.stereotype.Service
import java.util.concurrent.*

@Service
class OtherTaskService
{

    var runinglist = ArrayList<Any>()

    val queue = ThreadPoolExecutor(5, 10, 30,
            TimeUnit.MINUTES, LinkedBlockingDeque<Runnable>(50),
            ThreadPoolExecutor.CallerRunsPolicy())




    fun run(t:Runnable): Future<*>?
    {

        return queue.submit(t)
    }

}