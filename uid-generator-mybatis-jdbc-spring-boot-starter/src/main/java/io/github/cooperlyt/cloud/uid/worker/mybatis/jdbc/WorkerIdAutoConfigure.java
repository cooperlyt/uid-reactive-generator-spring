package io.github.cooperlyt.cloud.uid.worker.mybatis.jdbc;

import io.github.cooperlyt.cloud.uid.worker.mybatis.jdbc.mapper.JDBCWorkerNodeMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcOperations;


@ConditionalOnClass({JdbcOperations.class})
@Configuration
@MapperScan("io.github.cooperlyt.cloud.uid.worker.mybatis.jdbc.mapper")
public class WorkerIdAutoConfigure {

  private final JDBCWorkerNodeMapper jdbcWorkerNodeMapper;

  public WorkerIdAutoConfigure(JDBCWorkerNodeMapper jdbcWorkerNodeMapper) {
    this.jdbcWorkerNodeMapper = jdbcWorkerNodeMapper;
  }

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(){
    return new WorkerIdAssigner(jdbcWorkerNodeMapper);
  }

}
