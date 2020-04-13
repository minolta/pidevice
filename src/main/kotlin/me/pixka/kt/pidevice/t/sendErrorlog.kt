//package me.pixka.kt.pidevice.t
//
//import me.pixka.kt.pibase.t.HttpGetTask
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//import java.util.concurrent.Executors
//import java.util.concurrent.TimeUnit
//import java.util.logging.Logger
//
//@Component
//class sendErrorlog(val service: ErrorlogServiceII) {
//
//    private val logger = Logger.getLogger(sendErrorlog::class.java.getName())
//    @Scheduled(fixedDelay = 5000)
//    fun run() {
//
//
//        var list = service.nottoserver()
//
//        var target = System.getProperty("piserver") + "/errorlog/add"
//        if (list != null) {
//            logger.info("Found Error log ${list.size}")
//            var t = Executors.newSingleThreadExecutor()
//            for (e in list) {
//
//                var h = HttpGetTask(target)
//                var f = t.submit(h)
//
//                try {
//                    var re = f.get(5, TimeUnit.SECONDS)
//                    logger.info("Re :${re}" )
//                    e.toserver = true
//                    service.save(e)
//                    logger.info("save ${e}")
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    logger.severe(e.message)
//                }
//            }
//        }
//    }
//}