package com.example.spring_batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class SpringBatchApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringBatchApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchApplication.class, args);
	}

	@Bean
	public Job accountJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		Step accountStep = new StepBuilder("myStep", jobRepository)
						.tasklet((contribution, chunkContext) -> {
							log.info("hello !");
							return RepeatStatus.FINISHED;
						}, transactionManager)
						.build();
		return new JobBuilder("myJob", jobRepository)
				.start(accountStep)
				.build();
	}

	/**
	 * Contournement pour supprimer les warnings du BeanPostProcessor en attendant Spring Batch 5.2.0
	 *
	 * @see <a href="https://github.com/spring-projects/spring-batch/issues/4519">Multiple trationDelegate$BeanPostProcessorChecker Warnings Arise When Using Spring Boot 3.2.0</a>
	 * @see <a href="https://github.com/spring-projects/spring-batch/issues/4547">Revisit the mechanism of job registration</a>
	 *
	 * TODO : à enlever quand Spring Batch 5.2.0 (date de sortie prévue le 20 nov 2024) avec issue#4547 corrigée
	 */
	@Bean
	public static BeanDefinitionRegistryPostProcessor jobRegistryBeanPostProcessorRemover() {
		return registry -> registry.removeBeanDefinition("jobRegistryBeanPostProcessor");
	}
}
