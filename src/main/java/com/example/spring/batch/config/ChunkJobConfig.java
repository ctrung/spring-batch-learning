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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.spring.batch.config.CommonConfig.*;

/**
 * Job avec une étape de type chunk.
 * <br><br>
 * Chaque process et écriture prend 3 secondes pour démontrer le comportement séquentiel par défaut.
 * <br><br>
 * Pour des améliorations en temps d'exécution :
 * @see ChunkMultiThreadJobConfig
 */
@Configuration
public class ChunkJobConfig {

    private static final Logger log = LoggerFactory.getLogger(ChunkJobConfig.class);

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step chunkStep) {

        return new JobBuilder("chunkJob", jobRepository)
                .start(chunkStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step chunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          ItemReader<String> chunkReader,
                          ItemProcessor<String, String> chunkProcessor,
                          ItemWriter<String> chunkWriter) {

        return new StepBuilder("chunkStep", jobRepository)
                .<String, String>chunk(CHUNK_SIZE, transactionManager)
                .reader(chunkReader)
                .processor(chunkProcessor)
                .writer(chunkWriter)
                .build();
    }

    @Bean
    public ItemReader<String> chunkReader() {
        return new ListItemReader<>(ITEMS_SOURCE);
    }

    @Bean
    public ItemProcessor<String, String> chunkProcessor() {
        return s -> {
            log.info("Processing du chiffre {}", s);
            TimeUnit.SECONDS.sleep(PROCESS_TIME);
            return s.toUpperCase();
        };
    }

    @Bean
    public ItemWriter<String> chunkWriter() {
        return numbers -> {
            log.info("Ecriture des chiffres {}", numbers);
            TimeUnit.SECONDS.sleep(PROCESS_TIME);
            numbers.forEach(System.out::println);
        };
    }
}
