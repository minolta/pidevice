package me.pixka.pibase.s

import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.base.s.DefaultService
import me.pixka.kt.base.s.Ds
import me.pixka.kt.pibase.d.Logistate
import me.pixka.pibase.r.LogistateRepo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LogistateService(val r: LogistateRepo) : DefaultService<Logistate>() {


    fun findorcreate(n: Logistate): Logistate? {

        try {

            var lg: Logistate? = r.findByName(n.name!!)
            if (lg == null) {
                lg = Logistate()
                lg.name = n.name
                lg.refid = n.id
                lg = save(lg)
            }
            return lg
        } catch (e: Exception) {

            logger.error("[logistate] error : " + e.message)
            e.printStackTrace()
        }

        return null
    }

    fun findBtRefid(id: Long?): Logistate? {
        return r.findByRefid(id)
    }

    /**
     * กำหนด port ตามที่ส่งเข้ามา
     *
     * @param pin
     * port
     * @param status
     * state
     */
    fun setPort(pin: GpioPinDigitalOutput, status: Logistate) {
        setPort(pin, status.name)
    }

    fun setPort(pin: GpioPinDigitalOutput, p: String?) {
        if (p == "high") {
            pin.high()
        } else
            pin.low()
    }

    fun findorcreate(status: String): Logistate? {
        var lg: Logistate? = r.findByName(status)
        if (lg == null) {
            lg = Logistate()
            lg.name = status
            lg.refid = null
            lg = save(lg)
        }
        return lg
    }

    fun findByRefid(id: Long?): Logistate? {

        try {
            return r.findByRefid(id)
        } catch (e: Exception) {
            logger.error("findByRefid logistateservice " + e.message)
            e.printStackTrace()
        }

        return null
    }

    fun create(item: Logistate): Logistate? {
        try {
            val ls = Logistate()
            ls.refid = item.id
            ls.name = item.name
            return ls
        } catch (e: Exception) {
            logger.error("findByRefid create " + e.message)
            e.printStackTrace()
        }

        return null
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(LogistateService::class.java!!)
    }
}
