package com.example.spring.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.SynchronizedItemReader;
import org.springframework.batch.repeat.RepeatOperations;
import org.springframework.batch.repeat.support.TaskExecutorRepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

import static com.example.spring.batch.config.CommonConfig.*;

/**
 * Job avec une étape de type chunk. Les chunks sont multithreadés pour gagner en performance.
 * <br><br>
 * Attention : il faut s'assurer que les éléments du chunk soient threadsafe. Dans cet exemple, {@link ListItemReader}
 * est enveloppé par {@link SynchronizedItemReader} de telle sorte que chaque chiffre n'est lu qu'une seule fois.
 * <br><br>
 * Chaque process et écriture prend 3 secondes.
 */
@Configuration
public class ChunkMultiThreadJobConfig {

    private final Logger log = LoggerFactory.getLogger("ChunkMultiThreadJob");

    @Bean
    public Job chunkMultithreadedJob(JobRepository jobRepository, Step chunkMultithreadedStep) {

        return new JobBuilder("chunkMultithreadedJob", jobRepository)
                .start(chunkMultithreadedStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step chunkMultithreadedStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                       ItemReader<String> chunkMultithreadedReader,
                                       ItemProcessor<String, String> chunkMultithreadedProcessor,
                                       ItemWriter<String> chunkMultithreadedWriter,
                                       RepeatOperations multithreadedRepeatOperations,
                                       TaskExecutor taskExecutor) {

        return new StepBuilder("chunkMultithreadedStep", jobRepository)
                .<String, String>chunk(CHUNK_SIZE, transactionManager)
                .reader(chunkMultithreadedReader)
                .processor(chunkMultithreadedProcessor)
                .writer(chunkMultithreadedWriter)
                .stepOperations(multithreadedRepeatOperations)
                .build();
    }

    @Bean
    public ItemReader<String> chunkMultithreadedReader() {
        return new SynchronizedItemReader<>(new ListItemReader<>(ITEMS_SOURCE));
    }

    @Bean
    public ItemProcessor<String, String> chunkMultithreadedProcessor() {
        return s -> {
            log.info("Processing du chiffre {}", s);
            TimeUnit.SECONDS.sleep(PROCESS_TIME);
            return s.toUpperCase();
        };
    }

    @Bean
    public ItemWriter<String> chunkMultithreadedWriter() {
        return numbers -> {
            log.info("Ecriture des chiffres {}", numbers);
            TimeUnit.SECONDS.sleep(PROCESS_TIME);
            numbers.forEach(System.out::println);
        };
    }

    /**
     * @see <a href="https://github.com/spring-projects/spring-batch/issues/4389">issue#4389</a>
     * @see <a href="https://docs.spring.io/spring-batch/reference/scalability.html#multithreadedStep">Throttle limit deprecation</a>
     */
    @Bean
    public RepeatOperations multithreadedRepeatOperations(TaskExecutor taskExecutor) {

        TaskExecutorRepeatTemplate repeatTemplate = new TaskExecutorRepeatTemplate();
        repeatTemplate.setTaskExecutor(taskExecutor);
        repeatTemplate.setThrottleLimit(THROTTLE_LIMIT);
        return repeatTemplate;
    }
}
