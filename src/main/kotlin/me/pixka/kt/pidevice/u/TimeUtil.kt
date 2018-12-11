package me.pixka.kt.pidevice.u

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar



@Component
class TimeUtil() {

    //ใข้สำหรับดูว่า n อยู่ในช่วงของ s e หรือเปล่า
    fun checkInrang(s: Date, n: Date, e: Date): Boolean {
        try {
            var nvalue = n.time
            var today = todaydateonly()

            var svalue = today.time + s.time
            var evalue = today.time + e.time


            if (nvalue >= svalue && nvalue <= evalue)
                return true

        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false


    }

    fun checkInrangbyHighlow(s: Long, n: Long, e: Long): Boolean {
        try {

            var nn = Date().time
            logger.debug("get ${s} ${nn} ${e}")
            var nvalue = nn
            var today = todaydateonly()
            var todayvalue = today.time
            var svalue = todayvalue.plus(s)
            var evalue = todayvalue.plus(e)

            logger.debug("Check today ${today.time} s: [${svalue}] < n: [${nvalue}] > e: [${evalue}]")
            logger.debug("s < n?  ${nvalue - svalue}  n < e ${evalue - nvalue}  ${todayvalue + e}")
            if (nvalue >= svalue && nvalue <= evalue)
                return true

        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false


    }

    fun todaydateonly(): Date {
        var df = SimpleDateFormat("yyyy/MM/dd")
        df.timeZone = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.time = Date()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(TimeUtil::class.java)
    }

}