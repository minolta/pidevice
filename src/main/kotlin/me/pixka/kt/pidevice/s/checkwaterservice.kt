package me.pixka.kt.pidevice.s

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.Usewaterinformation
import me.pixka.kt.pibase.d.Waterinfo
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("pi", "lite")
class Checkwaterservice(val http: HttpControl, val io: Piio) {


    fun can(enddate: Date): Boolean {
        val mapper = ObjectMapper()
        var wi = Waterinfo()
        wi.mac = io.wifiMacAddress()
        wi.enduse = enddate

        var t = System.getProperty("piserver") + "/waterinfo"
        logger.debug("Call : ${t}")
        var entity: HttpEntity? = null

        try {
            var re = http.postJson(t, wi)
            entity = re.entity
        } catch (e: Exception) {
            logger.error("Can not connect service ${e.message}")
            return true
        }
        try {
            if (entity != null) {
                val response = EntityUtils.toString(entity)
                logger.debug("Can use water  : " + response)
                val ret = mapper.readValue(response, Usewaterinformation::class.java)
                logger.debug("waterinformation ${ret}")

                //ถ้าไม่มีข้อมูล
                if(ret.status != null && ret.status==500) {
                    logger.error("Some one use water ${ret}")
                    return false
                }
                logger.debug("suw Can use water Start use ${Date()} end ${ret.enduse}")
                return true
            }

        } catch (e: Exception) {
            logger.error("Convert error : ${e.message}")
        }


        logger.error("Can not use water")
        return false
    }


    companion object {
        internal var logger = LoggerFactory.getLogger(Checkwaterservice::class.java)
    }
}