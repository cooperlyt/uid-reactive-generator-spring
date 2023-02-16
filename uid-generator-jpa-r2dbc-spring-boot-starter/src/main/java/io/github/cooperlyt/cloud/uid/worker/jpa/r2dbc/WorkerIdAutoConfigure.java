package io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.repositories.WorkerNodeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@ConditionalOnClass(R2dbcEntityOperations.class)
@EnableR2dbcRepositories("io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.repositories")
@EntityScan("io.github.cooperlyt.cloud.uid.worker.jpa.r2dbc.entities")
@Configuration
public class WorkerIdAutoConfigure {

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
