package me.pixka.kt.pidevice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.TaskScheduler
import java.util.concurrent.Executor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = arrayOf("me.pixka"))
@EnableJpaRepositories(basePackages = arrayOf("me.pixka"))
@EntityScan(basePackages = arrayOf("me.pixka"))
class PideviceApplication
{

    @Bean
    fun taskScheduler(): ThreadPoolTaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 100
        return taskScheduler
    }

    @Bean
    fun pool(): ExecutorService? {
        val threadpool = Executors.newFixedThreadPool(20)
        return threadpool
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(PideviceApplication::class.java, *args)
}
