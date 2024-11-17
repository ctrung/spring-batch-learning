package com.example.spring.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Job composé de deux steps : un qui écrit en base et un autre qui lit.
 */
@Configuration
public class JdbcJobConfig {

    public static final int COMMIT_LIMIT = 500;
    public static final int MAX_NUMBER = 1_000;

    private final Logger log = LoggerFactory.getLogger("JdbcJob");

    @Bean
    public Job jdbcJob(JobRepository jobRepository, Step writeStep, Step readStep) {

        return new JobBuilder("jdbcJob", jobRepository)
                .start(writeStep)
                .next(readStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step writeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                          ItemWriter<Integer> jdbcWriter) {

        return new StepBuilder("writeStep", jobRepository)
                .<Integer, Integer>chunk(COMMIT_LIMIT, transactionManager)
                .reader(numberReader())
                .writer(jdbcWriter)
                .build();
    }

    @Bean
    public ItemReader<Integer> numberReader() {

        class NumberReader implements ItemReader<Integer> {

            private final AtomicInteger current = new AtomicInteger(0);

            @Override
            public Integer read() {
                if(current.get() == MAX_NUMBER) { return null; }
                return current.getAndAdd(1);
            }
        }

        return new NumberReader();
    }

    @Bean
    public ItemWriter<Integer> jdbcWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Integer>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO nombre (value)
                        VALUES (?)""")
                .itemPreparedStatementSetter( (i, ps) -> ps.setInt(1, i) )
                .build();
    }

    @Bean
    public Step readStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         ItemReader<Integer> jdbcReader) {

        return new StepBuilder("readStep", jobRepository)
                .<Integer, Integer>chunk(COMMIT_LIMIT, transactionManager)
                .reader(jdbcReader)
                .writer(printWriter())
                .build();
    }

    @Bean
    public ItemReader<Integer> jdbcReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Integer>()
                .name("jdbcReader")
                .dataSource(dataSource)
                .sql("""
                        SELECT value
                        FROM nombre""")
                .rowMapper((ResultSet rs, int index) -> rs.getInt(1))
                .build();
    }

    @Bean
    public ItemWriter<Integer> printWriter() {
        return numbers -> log.info("Affichage des chiffres {}", numbers);
    }
}
