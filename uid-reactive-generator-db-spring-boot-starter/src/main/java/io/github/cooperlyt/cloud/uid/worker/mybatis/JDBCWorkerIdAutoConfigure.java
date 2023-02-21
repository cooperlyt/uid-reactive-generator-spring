package io.github.cooperlyt.cloud.uid.worker.mybatis;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.jdbc.WorkerNodeMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


@ConditionalOnClass({SqlSessionFactory.class})
@ConditionalOnProperty(prefix = "spring.datasource",name = "url")
@Configuration
@MapperScan("io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.jdbc")
@AutoConfigureAfter(R2DBCWorkerIdAutoConfigure.class)
public class JDBCWorkerIdAutoConfigure {
  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(WorkerNodeIdent workerNodeIdent,
      WorkerNodeMapper workerNodeMapper){
    return new JDBCWorkerIdAssigner(workerNodeIdent,workerNodeMapper);
  }

}
