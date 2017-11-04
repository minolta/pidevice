package me.pixka.kt.pidevice.t

import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.ReadSensorService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/*
* ใช้สำหรับ หาค่า meทางานด้าน H
* */

@Component
class RunjobByH(val dhts:DhtvalueService,val sensorService: ReadSensorService) {


    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    fun run() {

        logger.info("Start RUN JOB By H")
      //  sensorService.readDhtvalue()
    }
    companion object {
        internal var logger = LoggerFactory.getLogger(RunjobByH::class.java)
    }
}