package com.example.spring.batch.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import javax.sql.DataSource;
import java.util.List;

/**
 * Configuration commune à tous les jobs.
 */
@Configuration
public class CommonConfig {

    /**
     * Pour les step de type chunk.
     */
    public static final int CHUNK_SIZE = 5;

    /**
     * Utile pour simuler un temps de travail.
     */
    public static final int PROCESS_TIME = 5;

    public static final int THROTTLE_LIMIT = 10;

    /**
     * Source de données.
     */
    public static final List<String> ITEMS_SOURCE =
            List.of("un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze");

    /**
     * Contournement pour supprimer les warnings du BeanPostProcessor en attendant Spring Batch 5.2.0
     * <br><br>
     * TODO : à enlever quand Spring Batch 5.2.0 (sortie fin nov 2024) avec issue#4547 corrigée
     * <br><br>
     * @see <a href="https://github.com/spring-projects/spring-batch/issues/4519">Multiple trationDelegate$BeanPostProcessorChecker Warnings Arise When Using Spring Boot 3.2.0</a>
     * @see <a href="https://github.com/spring-projects/spring-batch/issues/4547">Revisit the mechanism of job registration</a>
     */
    @Bean
    public BeanDefinitionRegistryPostProcessor jobRegistryBeanPostProcessorRemover() {
        return registry -> registry.removeBeanDefinition("jobRegistryBeanPostProcessor");
    }

    @Bean
    public TaskExecutor taskExecutor() {

//        return new SimpleAsyncTaskExecutor("spring_batch_");

//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(10);
//        taskExecutor.setMaxPoolSize(10);
//        taskExecutor.setQueueCapacity(25);
//        taskExecutor.setThreadNamePrefix("spring_batch_");
//        return taskExecutor;

        return new VirtualThreadTaskExecutor("spring_batch_");
    }

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("dataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.batch.datasource")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Les metadata Spring Batch sont dans une autre BDD.
     * @see <a href="https://docs.spring.io/spring-boot/how-to/batch.html#howto.batch.specifying-a-data-source">Specifying a Batch Data Source</a>
     */
    @Bean
    @BatchDataSource
    public DataSource batchDataSource(@Qualifier("batchDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }
}
