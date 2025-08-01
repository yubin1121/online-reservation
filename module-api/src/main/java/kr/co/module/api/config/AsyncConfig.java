package kr.co.module.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "productTaskExecutor")
    public Executor productTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);   // 기본 스레드 수
        executor.setMaxPoolSize(10);  // 최대 스레드 수
        executor.setQueueCapacity(25); // 큐 용량
        executor.setThreadNamePrefix("Product-Async-"); // 스레드 이름 접두사
        executor.initialize();
        return executor;
    }

    @Bean(name = "userQueryExecutor")
    public Executor userQueryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("UserQuery-Async-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "adminQueryExecutor")
    public Executor adminQueryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("AdminQuery-Async-");
        executor.initialize();
        return executor;
    }
}