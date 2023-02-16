package io.github.cooperlyt.cloud.uid.worker.jpa.jdbc;

import io.github.cooperlyt.cloud.uid.worker.jpa.jdbc.repositories.WorkerNodeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


import java.util.Map;
//extends HibernateJpaConfiguration
@Configuration
@EnableJpaRepositories
@EntityScan("io.github.cooperlyt.cloud.uid.worker.jpa.jdbc.entities")
public class WorkerIdAutoConfigure extends HibernateJpaAutoConfiguration {


  private final WorkerNodeRepository workerNodeRepository;

  public WorkerIdAutoConfigure(WorkerNodeRepository workerNodeRepository) {
    this.workerNodeRepository = workerNodeRepository;
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(){
    return new WorkerIdAssigner(workerNodeRepository);
  }

}
