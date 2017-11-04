package me.pixka.kt.pidevice.config

import me.pixka.kt.base.s.DbconfigService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * ใช้สำหรับ set default db config
 */
@Component
class ServerInitializer(val dbconfigService: DbconfigService) : ApplicationRunner {

    @Throws(Exception::class)
    override fun run(applicationArguments: ApplicationArguments) {

        //code goes here
        println("Start config")
        logger.info("Init dbconfig")
        dbconfigService.findorcreate("hosttarget", "http://pi1.pixka.me").value
      //  dbconfigService.findorcreate("serverds18addtarget", ":5002/ds18value/add").value
       dbconfigService.findorcreate("servercheck", ":5002/run").value
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(ServerInitializer::class.java)
    }
}


