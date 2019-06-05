package me.pixka.kt.pidevice

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


@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = arrayOf("me.pixka"))
@EnableJpaRepositories(basePackages = arrayOf("me.pixka"))
@EntityScan(basePackages = arrayOf("me.pixka"))
class PideviceApplication {

    @Bean
    fun taskScheduler(): ThreadPoolTaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 150
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
        val threadpool = ThreadPoolExecutor(50, 200, 1,
                TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>(400),
                ThreadPoolExecutor.CallerRunsPolicy())
        return threadpool
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(PideviceApplication::class.java, *args)
}
