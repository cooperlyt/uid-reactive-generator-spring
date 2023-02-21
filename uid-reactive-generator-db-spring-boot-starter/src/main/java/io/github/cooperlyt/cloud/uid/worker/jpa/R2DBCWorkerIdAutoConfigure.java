package io.github.cooperlyt.cloud.uid.worker.jpa;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.jpa.repositories.r2dbc.WorkerNodeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@ConditionalOnClass(R2dbcEntityOperations.class)
@ConditionalOnProperty(prefix = "spring.r2dbc",name = "url")
@EnableR2dbcRepositories("io.github.cooperlyt.cloud.uid.worker.jpa.repositories.r2dbc")
@EntityScan("io.github.cooperlyt.cloud.uid.worker.entities")
@Configuration
public class R2DBCWorkerIdAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(WorkerNodeIdent workerNodeIdent,
                                    WorkerNodeRepository workerNodeRepository){
    return new R2DBCWorkerIdAssigner(workerNodeIdent,workerNodeRepository);
  }
}
