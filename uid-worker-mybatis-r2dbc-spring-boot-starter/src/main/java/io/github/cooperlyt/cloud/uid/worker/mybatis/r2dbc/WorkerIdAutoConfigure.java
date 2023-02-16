package io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc;

import io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc.mapper.R2dbcWorkerNodeMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation.R2dbcMapperScan;

@ConditionalOnClass(R2dbcEntityOperations.class)
@Configuration
@R2dbcMapperScan("io.github.cooperlyt.cloud.uid.worker.mybatis.r2dbc.mapper")
public class WorkerIdAutoConfigure {

  private final R2dbcWorkerNodeMapper r2dbcWorkerNodeMapper;

  public WorkerIdAutoConfigure(R2dbcWorkerNodeMapper r2dbcWorkerNodeMapper) {
    this.r2dbcWorkerNodeMapper = r2dbcWorkerNodeMapper;
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(){
    return new WorkerIdAssigner(r2dbcWorkerNodeMapper);
  }
}
