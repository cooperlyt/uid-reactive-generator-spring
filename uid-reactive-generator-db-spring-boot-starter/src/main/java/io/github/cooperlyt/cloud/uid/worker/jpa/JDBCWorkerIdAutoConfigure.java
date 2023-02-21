package io.github.cooperlyt.cloud.uid.worker.jpa;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.jpa.repositories.jdbc.WorkerNodeRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.JpaRepositoryNameSpaceHandler;

//extends HibernateJpaConfiguration
@Configuration
@ConditionalOnClass(JpaRepositoryNameSpaceHandler.class)
@ConditionalOnProperty(prefix = "spring.datasource",name = "url")
@EnableJpaRepositories("io.github.cooperlyt.cloud.uid.worker.jpa.repositories.jdbc")
@EntityScan("io.github.cooperlyt.cloud.uid.worker.entities")
@AutoConfigureAfter({R2DBCWorkerIdAutoConfigure.class})
public class JDBCWorkerIdAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(WorkerNodeIdent workerNodeIdent,
                                    WorkerNodeRepository workerNodeRepository){
    return new JDBCWorkerIdAssigner(workerNodeIdent,workerNodeRepository);
  }

}
