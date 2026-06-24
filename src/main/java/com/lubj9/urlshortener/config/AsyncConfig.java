package com.lubj9.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

/**
 * Pool de threads para tarefas assíncronas (tracking de cliques).
 *
 * Usa Virtual Threads do Java 21 — ideais para I/O-bound como gravar
 * uma linha de clique no banco. Cada thread virtual consome alguns kilobytes,
 * permitindo milhares simultâneas sem preocupação de exaustão do pool.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
