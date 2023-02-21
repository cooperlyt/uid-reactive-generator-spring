package io.github.cooperlyt.cloud.uid.worker.mybatis;

import io.github.cooperlyt.cloud.uid.worker.WorkerIdAssigner;
import io.github.cooperlyt.cloud.uid.worker.WorkerNodeIdent;
import io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.r2dbc.WorkerNodeMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.MybatisReactiveContextManager;
import pro.chenggang.project.reactive.mybatis.support.r2dbc.spring.annotation.R2dbcMapperScan;

@ConditionalOnClass(MybatisReactiveContextManager.class)
@ConditionalOnProperty(prefix = "spring.r2dbc.mybatis",name = "r2dbc-url")
@Configuration
@R2dbcMapperScan("io.github.cooperlyt.cloud.uid.worker.mybatis.mapper.r2dbc")
public class R2DBCWorkerIdAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  @Lazy
  WorkerIdAssigner workerIdAssigner(WorkerNodeIdent workerNodeIdent,
                                    WorkerNodeMapper r2dbcWorkerNodeMapper){
    return new R2DBCWorkerIdAssigner(workerNodeIdent,r2dbcWorkerNodeMapper);
  }
}
