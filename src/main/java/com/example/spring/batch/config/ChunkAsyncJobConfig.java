package com.example.spring.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.example.spring.batch.config.CommonConfig.*;

/**
 * Job avec une étape de type chunk. Le processor et le writer sont asynchrones pour gagner en performance.
 * <br><br>
 * Attention : il faut s'assurer que les éléments du chunk soient threadsafe. 
 */
@Configuration
public class ChunkAsyncJobConfig {

    private static final Logger log = LoggerFactory.getLogger("ChunkAsyncJob");

    @Bean
    public Job chunkAsyncJob(JobRepository jobRepository, Step chunkAsyncStep) {

        return new JobBuilder("chunkAsyncJob", jobRepository)
                .start(chunkAsyncStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step chunkAsyncStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                               AsyncItemProcessor<String, String> chunkAsyncProcessor) {

        return new StepBuilder("chunkAsyncStep", jobRepository)
                .<String, Future<String>>chunk(CHUNK_SIZE, transactionManager)
                .reader(chunkAsyncReader())
                .processor(chunkAsyncProcessor)
                .writer(chunkAsyncWriter())
                .build();
    }

    @Bean
    public ItemReader<String> chunkAsyncReader() {
        return new ListItemReader<>(ITEMS_SOURCE);
    }

    @Bean
    public AsyncItemProcessor<String, String> chunkAsyncProcessor(TaskExecutor taskExecutor) {

        ItemProcessor<String, String> processor = s -> {
            log.info("Processing du chiffre {}", s);
            TimeUnit.SECONDS.sleep(PROCESS_TIME);
            return s.toUpperCase();
        };

        AsyncItemProcessor<String, String> asyncProcessor = new AsyncItemProcessor<>();
        asyncProcessor.setDelegate(processor);
        asyncProcessor.setTaskExecutor(taskExecutor);

        return asyncProcessor;
    }

    @Bean
    public AsyncItemWriter<String> chunkAsyncWriter() {

        ItemWriter<String> writer = numbers -> {
            log.info("Ecriture des chiffres {}", numbers);
            TimeUnit.SECONDS.sleep(PROCESS_TIME);
            numbers.forEach(System.out::println);
        };

        AsyncItemWriter<String> asyncWriter = new AsyncItemWriter<>();
        asyncWriter.setDelegate(writer);

        return asyncWriter;
    }
}
