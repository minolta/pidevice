package me.pixka.kt.pidevice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.*


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
        taskScheduler.poolSize = 100
        return taskScheduler
    }

    @Bean(name = arrayOf("aa"))
    fun threadPoolTaskExecutor(): Executor {
        return ThreadPoolExecutor(10, 20, 30,
                TimeUnit.MINUTES, LinkedBlockingDeque<Runnable>(50),
                ThreadPoolExecutor.CallerRunsPolicy())

    }

    @Bean(name = arrayOf("longrun"))
    fun longrun(): Executor {
        return ThreadPoolExecutor(5, 25, 30,
                TimeUnit.HOURS, LinkedBlockingDeque<Runnable>(50),
                ThreadPoolExecutor.CallerRunsPolicy())

    }
    @Bean(name = arrayOf("pool"))
    fun pool(): ExecutorService? {
        val threadpool = ThreadPoolExecutor(20, 25, 30,
                TimeUnit.MINUTES, LinkedBlockingDeque<Runnable>(50),
                ThreadPoolExecutor.CallerRunsPolicy())


        //Executors.newFixedThreadPool(20)
        return threadpool
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(PideviceApplication::class.java, *args)
}
