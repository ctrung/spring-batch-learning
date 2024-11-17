package com.example.spring.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job avec une étape de type tasklet.
 */
@Configuration
public class TaskletJobConfig {

    private static final Logger log = LoggerFactory.getLogger(TaskletJobConfig.class);

    @Bean
    public Job taskletJob(JobRepository jobRepository, Step taskletStep) {

        return new JobBuilder("taskletJob", jobRepository)
                .start(taskletStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        Tasklet tasklet = (contribution, chunkContext) -> {
            log.info("Hello tasklet !");
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("taskletStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
