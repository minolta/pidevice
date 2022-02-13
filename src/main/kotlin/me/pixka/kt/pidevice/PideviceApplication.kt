package me.pixka.kt.pidevice

import me.pixka.kt.pidevice.s.WarterLowPressureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.*
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy
import java.util.TimeZone
import javax.annotation.PostConstruct


@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = arrayOf("me.pixka"))
//@EnableJpaRepositories(basePackages = arrayOf("me.pixka"))
//@EntityScan(basePackages = arrayOf("me.pixka"))
class PideviceApplication {

    @Bean
    fun taskScheduler(): ThreadPoolTaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 250
        taskScheduler.threadNamePrefix = "SS-"

        return taskScheduler
    }

    @Bean(name = arrayOf("aa"))
    fun threadPoolTaskExecutor(): Executor {

        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 25
        executor.setAllowCoreThreadTimeOut(true)
        executor.keepAliveSeconds = 10
        executor.setQueueCapacity(500)
        executor.threadNamePrefix = "AA-"
        executor.initialize()
        return executor

    }

    @Bean(name = arrayOf("pt"))
    fun pt(): Executor {

        return ThreadPoolExecutor(
                10,
                25,
                5, // <--- The keep alive for the async task
                TimeUnit.SECONDS, // <--- TIMEOUT IN SECONDS
                ArrayBlockingQueue(100),
                AbortPolicy() // <-- It will abort if timeout exceeds
        )
    }
    @Bean(name = arrayOf("pool"))
    fun pool(): ExecutorService? {
        var coresize = 50
        var maxpool = 200
        var sc = System.getProperty("coresize")
        if (sc != null)
            coresize = sc.toInt()
        var mp = System.getProperty("maxpoolsize")
        if (mp != null)
            maxpool = mp.toInt()
        return Executors.newFixedThreadPool(coresize)
    }

    @Bean(name = arrayOf("pool2"))
    fun p(): ExecutorService? {
        return Executors.newFixedThreadPool(20)
    }

    @Autowired
    lateinit var ws:WarterLowPressureService
    @PostConstruct
    fun init() {
        // Setting Spring Boot SetTimeZone
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        ws.setDefaultMaxCount()
        println("Config Max count ${ws.maxcount}")
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(PideviceApplication::class.java, *args)
}
